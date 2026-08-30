package com.example.order.service.messaging;

import com.example.order.service.config.RabbitMQConfig;
import com.example.order.service.entity.OutboxEvent;
import com.example.order.service.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final int BATCH_SIZE = 20;
    private static final int MAX_ATTEMPTS = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:3000}")
    @Transactional
    public void publishPendingEvents() {

        LocalDateTime now = LocalDateTime.now();

        List<OutboxEvent> events =
                outboxEventRepository.findPendingEvents(
                        now,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        try {

            OrderCreatedEvent.Payload payload =
                    objectMapper.readValue(
                            event.getPayload(),
                            OrderCreatedEvent.Payload.class
                    );

            OrderCreatedEvent message =
                    new OrderCreatedEvent(
                            event.getId(),
                            event.getEventType(),
                            event.getAggregateId(),
                            payload.getUserId(),
                            LocalDateTime.now(),
                            event.getId().toString(),
                            payload
                    );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                    message
            );

            event.markPublished();

            outboxEventRepository.save(event);

        } catch (Exception ex) {

            event.markFailed(
                    ex.getMessage(),
                    MAX_ATTEMPTS
            );

            outboxEventRepository.save(event);
        }
    }
}