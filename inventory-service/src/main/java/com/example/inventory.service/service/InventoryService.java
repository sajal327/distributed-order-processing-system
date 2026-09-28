package com.example.inventory.service.service;

import com.example.inventory.service.config.RabbitMQConfig;
import com.example.inventory.service.entity.Inventory;
import com.example.inventory.service.entity.InventoryInboxEvent;
import com.example.inventory.service.entity.InventoryOutboxEvent;
import com.example.inventory.service.messaging.InventoryFailedEvent;
import com.example.inventory.service.messaging.InventoryReservedEvent;
import com.example.inventory.service.messaging.OrderCreatedEvent;
import com.example.inventory.service.repository.InventoryOutboxRepository;
import com.example.inventory.service.repository.InventoryInboxRepository;
import com.example.inventory.service.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryOutboxRepository outboxRepository;
    private final InventoryInboxRepository inboxRepository;
    private final ObjectMapper objectMapper;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryOutboxRepository outboxRepository,
            InventoryInboxRepository inboxRepository,
            ObjectMapper objectMapper
    ) {
        this.inventoryRepository = inventoryRepository;
        this.outboxRepository = outboxRepository;
        this.inboxRepository = inboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(
            rollbackFor = Exception.class
    )
    public void reserveInventory(
            OrderCreatedEvent event
    ) {
        if (inboxRepository.existsByEventId(event.eventId())) {
            return;
        }
        for (OrderCreatedEvent.Item item :
                event.payload().items()) {

            Inventory inventory =
                    inventoryRepository
                            .findByProductId(item.productId())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Inventory not found for product: "
                                                    + item.productId()
                                    ));

            inventory.reserve(item.quantity());

            inventoryRepository.save(inventory);
        }

        InventoryReservedEvent reservedEvent =
                new InventoryReservedEvent(
                        event.eventId(),
                        "INVENTORY_RESERVED",
                        event.orderId(),
                        event.userId(),
                        event.payload().totalAmount(),
                        "INR",
                        LocalDateTime.now(),
                        event.correlationId(),
                        event.eventId()
                );

        saveOutboxEvent(
                event,
                "INVENTORY_RESERVED",
                RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY,
                reservedEvent
        );
        InventoryInboxEvent inboxEvent =
                new InventoryInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                );

        inboxRepository.save(inboxEvent);
    }

    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {

        if (inboxRepository.existsByEventId(event.eventId())) {
            return;
        }

        for (OrderCreatedEvent.Item item : event.payload().items()) {

            Inventory inventory =
                    inventoryRepository
                            .findByProductId(item.productId())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Inventory not found for product: "
                                                    + item.productId()
                                    ));

            inventory.reserve(item.quantity());

            inventoryRepository.save(inventory);
        }

        InventoryReservedEvent reservedEvent =
                new InventoryReservedEvent(
                        event.eventId(),
                        "INVENTORY_RESERVED",
                        event.orderId(),
                        event.userId(),
                        event.payload().totalAmount(),
                        "INR",
                        LocalDateTime.now(),
                        event.correlationId(),
                        event.eventId()
                );

        saveOutboxEvent(
                event,
                "INVENTORY_RESERVED",
                RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY,
                reservedEvent
        );

        inboxRepository.save(
                new InventoryInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                )
        );
    }

    @Transactional
    public void processOrderCreatedFailure(
            OrderCreatedEvent event,
            String reason
    ) {

        if (inboxRepository.existsByEventId(event.eventId())) {
            return;
        }

        InventoryFailedEvent failedEvent =
                new InventoryFailedEvent(
                        event.eventId(),
                        "INVENTORY_FAILED",
                        event.orderId(),
                        event.userId(),
                        LocalDateTime.now(),
                        event.correlationId(),
                        event.eventId(),
                        reason
                );

        saveOutboxEvent(
                event,
                "INVENTORY_FAILED",
                RabbitMQConfig.INVENTORY_FAILED_ROUTING_KEY,
                failedEvent
        );

        inboxRepository.save(
                new InventoryInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                )
        );
    }

    @Transactional
    public void markInventoryFailed(
            OrderCreatedEvent event,
            String reason
    ) {

        InventoryFailedEvent failedEvent =
                new InventoryFailedEvent(
                        event.eventId(),
                        "INVENTORY_FAILED",
                        event.orderId(),
                        event.userId(),
                        LocalDateTime.now(),
                        event.correlationId(),
                        event.eventId(),
                        reason
                );

        saveOutboxEvent(
                event,
                "INVENTORY_FAILED",
                RabbitMQConfig.INVENTORY_FAILED_ROUTING_KEY,
                failedEvent
        );
    }

    private void saveOutboxEvent(
            OrderCreatedEvent sourceEvent,
            String eventType,
            String routingKey,
            Object event
    ) {

        try {

            String payload =
                    objectMapper.writeValueAsString(event);

            InventoryOutboxEvent outboxEvent =
                    new InventoryOutboxEvent(
                            sourceEvent.eventId(),
                            "ORDER",
                            sourceEvent.orderId(),
                            sourceEvent.correlationId(),
                            eventType,
                            routingKey,
                            payload
                    );

            outboxRepository.save(outboxEvent);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Failed to serialize inventory outbox event",
                    ex
            );
        }
    }
}




//package com.example.inventory.service.service;
//
//import com.example.inventory.service.config.RabbitMQConfig;
//import com.example.inventory.service.entity.Inventory;
//import com.example.inventory.service.entity.InventoryOutboxEvent;
//import com.example.inventory.service.messaging.InventoryFailedEvent;
//import com.example.inventory.service.messaging.InventoryReservedEvent;
//import com.example.inventory.service.messaging.OrderCreatedEvent;
//import com.example.inventory.service.repository.InventoryOutboxRepository;
//import com.example.inventory.service.repository.InventoryRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//public class InventoryService {
//
//    private final InventoryRepository inventoryRepository;
//    private final InventoryOutboxRepository outboxRepository;
//    private final ObjectMapper objectMapper;
//
//    public InventoryService(
//            InventoryRepository inventoryRepository,
//            InventoryOutboxRepository outboxRepository,
//            ObjectMapper objectMapper
//    ) {
//        this.inventoryRepository = inventoryRepository;
//        this.outboxRepository = outboxRepository;
//        this.objectMapper = objectMapper;
//    }
//
//    @Transactional
//    public void reserveInventory(OrderCreatedEvent event) {
//
//        for (OrderCreatedEvent.Item item : event.payload().items()) {
//
//            Inventory inventory =
//                    inventoryRepository
//                            .findByProductId(item.productId())
//                            .orElseThrow(() ->
//                                    new IllegalStateException(
//                                            "Inventory not found for product: "
//                                                    + item.productId()
//                                    )
//                            );
//
//            inventory.reserve(item.quantity());
//
//            inventoryRepository.save(inventory);
//        }
//
//        InventoryReservedEvent reservedEvent =
//                new InventoryReservedEvent(
//                        event.eventId(),
//                        "INVENTORY_RESERVED",
//                        event.orderId(),
//                        event.userId(),
//                        LocalDateTime.now(),
//                        event.correlationId()
//                );
//
//        saveOutboxEvent(
//                event,
//                "INVENTORY_RESERVED",
//                RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY,
//                reservedEvent
//        );
//    }
//
//    @Transactional
//    public void markInventoryFailed(
//            OrderCreatedEvent event,
//            String reason
//    ) {
//
//        InventoryFailedEvent failedEvent =
//                new InventoryFailedEvent(
//                        event.eventId(),
//                        "INVENTORY_FAILED",
//                        event.orderId(),
//                        event.userId(),
//                        LocalDateTime.now(),
//                        event.correlationId(),
//                        reason
//                );
//
//        saveOutboxEvent(
//                event,
//                "INVENTORY_FAILED",
//                RabbitMQConfig.INVENTORY_FAILED_ROUTING_KEY,
//                failedEvent
//        );
//    }
//
//    private void saveOutboxEvent(
//            OrderCreatedEvent sourceEvent,
//            String eventType,
//            String routingKey,
//            Object event
//    ) {
//
//        try {
//
//            String payload =
//                    objectMapper.writeValueAsString(event);
//
//            InventoryOutboxEvent outboxEvent =
//                    new InventoryOutboxEvent(
//                            sourceEvent.eventId(),
//                            "ORDER",
//                            sourceEvent.orderId(),
//                            eventType,
//                            routingKey,
//                            payload
//                    );
//
//            outboxRepository.save(outboxEvent);
//
//        } catch (JsonProcessingException ex) {
//            throw new IllegalStateException(
//                    "Failed to serialize inventory outbox event",
//                    ex
//            );
//        }
//    }
//}


//package com.example.inventory.service.service;
//
//import com.example.inventory.service.entity.Inventory;
//import com.example.inventory.service.messaging.InventoryReservationResult;
//import com.example.inventory.service.messaging.OrderCreatedEvent;
//import com.example.inventory.service.repository.InventoryRepository;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class InventoryService {
//
//    private final InventoryRepository inventoryRepository;
//    private final ApplicationEventPublisher applicationEventPublisher;
//
//    public InventoryService(
//            InventoryRepository inventoryRepository,
//            ApplicationEventPublisher applicationEventPublisher
//    ) {
//        this.inventoryRepository = inventoryRepository;
//        this.applicationEventPublisher = applicationEventPublisher;
//    }
//
//    @Transactional
//    public void reserveInventory(OrderCreatedEvent event) {
//
//        try {
//
//            for (OrderCreatedEvent.Item item : event.payload().items()) {
//
//                Inventory inventory = inventoryRepository
//                        .findByProductId(item.productId())
//                        .orElseThrow(() -> new IllegalStateException(
//                                "Inventory not found for product: "
//                                        + item.productId()
//                        ));
//
//                inventory.reserve(item.quantity());
//
//                inventoryRepository.save(inventory);
//            }
//
//            applicationEventPublisher.publishEvent(
//                    new InventoryReservationResult(
//                            event,
//                            true,
//                            null
//                    )
//            );
//
//        } catch (RuntimeException ex) {
//
//            applicationEventPublisher.publishEvent(
//                    new InventoryReservationResult(
//                            event,
//                            false,
//                            ex.getMessage()
//                    )
//            );
//
//            throw ex;
//        }
//    }
//}


//package com.example.inventory.service.service;
//
//import com.example.inventory.service.entity.Inventory;
//import com.example.inventory.service.messaging.OrderCreatedEvent;
//import com.example.inventory.service.repository.InventoryRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class InventoryService {
//
//    private final InventoryRepository inventoryRepository;
//
//    public InventoryService(InventoryRepository inventoryRepository) {
//        this.inventoryRepository = inventoryRepository;
//    }
//
//    @Transactional
//    public void reserveInventory(OrderCreatedEvent event) {
//
//        for (OrderCreatedEvent.Item item : event.payload().items()) {
//
//            Inventory inventory = inventoryRepository
//                    .findByProductId(item.productId())
//                    .orElseThrow(() -> new IllegalStateException(
//                            "Inventory not found for product: " + item.productId()
//                    ));
//
//            inventory.reserve(item.quantity());
//
//            inventoryRepository.save(inventory);
//        }
//    }
//}