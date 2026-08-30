package com.example.order.service.service;

import com.example.order.service.dto.CreateOrderRequest;
import com.example.order.service.dto.OrderItemRequest;
import com.example.order.service.dto.OrderResponse;
import com.example.order.service.entity.IdempotencyKey;
import com.example.order.service.entity.Order;
import com.example.order.service.entity.OrderItem;
import com.example.order.service.entity.OutboxEvent;
import com.example.order.service.entity.Product;
import com.example.order.service.entity.User;
import com.example.order.service.repository.IdempotencyKeyRepository;
import com.example.order.service.repository.OrderRepository;
import com.example.order.service.repository.OutboxEventRepository;
import com.example.order.service.repository.ProductRepository;
import com.example.order.service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.order.service.messaging.OrderCreatedEvent;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            OutboxEventRepository outboxEventRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request,
            String idempotencyKey
    ) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + request.getUserId()
                        )
                );

        String requestHash = calculateRequestHash(request);

        var existingKey =
                idempotencyKeyRepository
                        .findByUserIdAndIdempotencyKey(
                                user.getId(),
                                idempotencyKey
                        );

        if (existingKey.isPresent()) {

            IdempotencyKey storedKey = existingKey.get();

            if (!storedKey.getRequestHash().equals(requestHash)) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used with a different request"
                );
            }

            if (storedKey.getResponseBody() != null) {
                try {
                    return objectMapper.readValue(
                            storedKey.getResponseBody(),
                            OrderResponse.class
                    );
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(
                            "Unable to deserialize stored idempotent response",
                            e
                    );
                }
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository
                    .findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Product not found: "
                                            + itemRequest.getProductId()
                            )
                    );

            BigDecimal unitPrice = product.getPrice();

            BigDecimal itemTotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());

            /*
             * Important:
             * Store the product price at order creation time.
             */
            orderItem.setUnitPrice(unitPrice);

            order.addItem(orderItem);

            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        /*
         * Create the transactional outbox event.
         *
         * Because this method is @Transactional,
         * the order and outbox event are committed
         * in the same database transaction.
         */
        createOutboxEvent(savedOrder);

        OrderResponse response = OrderResponse.from(savedOrder);

        saveIdempotencyRecord(
                user,
                idempotencyKey,
                requestHash,
                response
        );

        return response;
    }

    private void createOutboxEvent(Order order) {

        try {

            List<OrderCreatedEvent.Payload.Item> items =
                    order.getItems()
                            .stream()
                            .map(item ->
                                    new OrderCreatedEvent.Payload.Item(
                                            item.getProduct().getId(),
                                            item.getQuantity(),
                                            item.getUnitPrice()
                                    )
                            )
                            .toList();

            OrderCreatedEvent.Payload payload =
                    new OrderCreatedEvent.Payload(
                            order.getId(),
                            order.getUser().getId(),
                            order.getStatus(),
                            order.getTotalAmount(),
                            items
                    );

            String payloadJson =
                    objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent();

            outboxEvent.setAggregateType("ORDER");
            outboxEvent.setAggregateId(order.getId());
            outboxEvent.setEventType("OrderCreated");
            outboxEvent.setPayload(payloadJson);
            outboxEvent.setStatus("PENDING");
            outboxEvent.setAttempts(0);
            outboxEvent.setNextAttemptAt(LocalDateTime.now());

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Unable to create order outbox event",
                    e
            );
        }
    }

    private void saveIdempotencyRecord(
            User user,
            String idempotencyKey,
            String requestHash,
            OrderResponse response
    ) {

        try {

            IdempotencyKey record = new IdempotencyKey();

            record.setIdempotencyKey(idempotencyKey);
            record.setUser(user);
            record.setRequestHash(requestHash);
            record.setResponseStatus(201);
            record.setResponseBody(
                    objectMapper.writeValueAsString(response)
            );

            /*
             * Keep the idempotency record for 24 hours.
             */
            record.setExpiresAt(
                    LocalDateTime.now().plusHours(24)
            );

            idempotencyKeyRepository.save(record);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Unable to store idempotent response",
                    e
            );
        }
    }

    private String calculateRequestHash(
            CreateOrderRequest request
    ) {

        try {

            String canonicalRequest =
                    objectMapper.writeValueAsString(request);

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    canonicalRequest.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (JsonProcessingException | NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "Unable to calculate request hash",
                    e
            );
        }
    }
}