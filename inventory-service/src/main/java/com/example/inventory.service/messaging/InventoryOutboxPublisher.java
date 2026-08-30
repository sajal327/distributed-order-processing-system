package com.example.inventory.service.messaging;

import com.example.inventory.service.config.RabbitMQConfig;
import com.example.inventory.service.entity.InventoryOutboxEvent;
import com.example.inventory.service.repository.InventoryOutboxRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class InventoryOutboxPublisher {

    private static final int BATCH_SIZE = 20;
    private static final int MAX_ATTEMPTS = 5;

    private final InventoryOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryOutboxPublisher(
            InventoryOutboxRepository outboxRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(
            fixedDelayString =
                    "${inventory.outbox.publisher.delay-ms:3000}"
    )
    public void publishPendingEvents() {

        LocalDateTime now = LocalDateTime.now();

        List<InventoryOutboxEvent> events =
                outboxRepository.findPendingEvents(
                        now,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (InventoryOutboxEvent event : events) {
            publishEvent(event);
        }
    }

    protected void publishEvent(
            InventoryOutboxEvent event
    ) {

        try {

            Message message =
                    MessageBuilder
                            .withBody(
                                    event.getPayload()
                                            .getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                            )
                            .setContentType(
                                    MessageProperties.CONTENT_TYPE_JSON
                            )
                            .setHeader(
                                    "eventId",
                                    event.getEventId()
                            )
                            .setHeader(
                                    "eventType",
                                    event.getEventType()
                            )
                            .setHeader(
                                    "correlationId",
                                    event.getCorrelationId()
                            )
                            .build();

            rabbitTemplate.send(
                    RabbitMQConfig.INVENTORY_EVENTS_EXCHANGE,
                    event.getRoutingKey(),
                    message
            );

            event.markPublished();

            outboxRepository.save(event);

        } catch (Exception ex) {

            event.markFailed(
                    ex.getMessage(),
                    MAX_ATTEMPTS
            );

            outboxRepository.save(event);
        }
    }
}


//package com.example.inventory.service.messaging;
//
//import com.example.inventory.service.config.RabbitMQConfig;
//import com.example.inventory.service.entity.InventoryOutboxEvent;
//import com.example.inventory.service.repository.InventoryOutboxRepository;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//public class InventoryOutboxPublisher {
//
//    private static final int BATCH_SIZE = 20;
//    private static final int MAX_ATTEMPTS = 5;
//
//    private final InventoryOutboxRepository outboxRepository;
//    private final RabbitTemplate rabbitTemplate;
//
//    public InventoryOutboxPublisher(
//            InventoryOutboxRepository outboxRepository,
//            RabbitTemplate rabbitTemplate
//    ) {
//        this.outboxRepository = outboxRepository;
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    @Scheduled(
//            fixedDelayString =
//                    "${inventory.outbox.publisher.delay-ms:3000}"
//    )
//    public void publishPendingEvents() {
//
//        LocalDateTime now = LocalDateTime.now();
//
//        List<InventoryOutboxEvent> events =
//                outboxRepository.findPendingEvents(
//                        now,
//                        PageRequest.of(0, BATCH_SIZE)
//                );
//
//        for (InventoryOutboxEvent event : events) {
//            publishEvent(event);
//        }
//    }
//
//    @Transactional
//    protected void publishEvent(
//            InventoryOutboxEvent event
//    ) {
//
//        try {
//
//            rabbitTemplate.convertAndSend(
//                    RabbitMQConfig.INVENTORY_EVENTS_EXCHANGE,
//                    event.getRoutingKey(),
//                    event.getPayload()
//            );
//
//            event.markPublished();
//
//            outboxRepository.save(event);
//
//        } catch (Exception ex) {
//
//            event.markFailed(
//                    ex.getMessage(),
//                    MAX_ATTEMPTS
//            );
//
//            outboxRepository.save(event);
//        }
//    }
//}