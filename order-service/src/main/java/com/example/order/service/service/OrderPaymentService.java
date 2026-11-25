
package com.example.order.service.service;

import com.example.order.service.entity.Order;
import com.example.order.service.entity.OrderInboxEvent;
import com.example.order.service.messaging.PaymentCompletedEvent;
import com.example.order.service.messaging.PaymentFailedEvent;
import com.example.order.service.repository.OrderInboxRepository;
import com.example.order.service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final OrderInboxRepository inboxRepository;

    public OrderPaymentService(
            OrderRepository orderRepository,
            OrderInboxRepository inboxRepository
    ) {
        this.orderRepository = orderRepository;
        this.inboxRepository = inboxRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPaymentCompleted(
            PaymentCompletedEvent event
    ) {

        validate(event.eventId(), event.orderId());

        if (inboxRepository.existsByEventId(event.eventId())) {
            return;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found: " + event.orderId()
                ));

        if ("PENDING".equals(order.getStatus())) {
            order.setStatus("CONFIRMED");
            orderRepository.save(order);

        } else if (!"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Cannot confirm order " + event.orderId()
                            + " from status " + order.getStatus()
            );
        }

        inboxRepository.save(
                new OrderInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                )
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPaymentFailed(
            PaymentFailedEvent event
    ) {

        validate(event.eventId(), event.orderId());

        if (inboxRepository.existsByEventId(event.eventId())) {
            return;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found: " + event.orderId()
                ));

        if ("PENDING".equals(order.getStatus())) {
            order.setStatus("PAYMENT_FAILED");
            orderRepository.save(order);

        } else if (!"PAYMENT_FAILED".equals(order.getStatus())) {
            throw new IllegalStateException(
                    "Cannot fail order " + event.orderId()
                            + " from status " + order.getStatus()
            );
        }

        inboxRepository.save(
                new OrderInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                )
        );
    }

    private void validate(Long eventId, Long orderId) {

        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Payment event ID cannot be null"
            );
        }

        if (orderId == null) {
            throw new IllegalArgumentException(
                    "Payment event order ID cannot be null"
            );
        }
    }
}
