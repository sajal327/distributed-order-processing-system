package com.example.notification.service.messaging;

import com.example.notification.service.config.RabbitMQConfig;
import com.example.notification.service.domain.NotificationOutboxEvent;
import com.example.notification.service.repository.NotificationOutboxRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationOutboxPublisher {

    private static final int BATCH_SIZE = 50;

    private final NotificationOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public NotificationOutboxPublisher(
            NotificationOutboxRepository outboxRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<NotificationOutboxEvent> events =
                outboxRepository.findPendingEvents(
                        LocalDateTime.now(),
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (NotificationOutboxEvent event : events) {
            publishEvent(event);
        }
    }

    public void publishEvent(
            NotificationOutboxEvent event
    ) {

        try {

            rabbitTemplate.convertAndSend(
                    "notification.events",
                    event.getRoutingKey(),
                    event.getPayload(),
                    message -> {

                        message.getMessageProperties()
                                .setHeader(
                                        "eventId",
                                        event.getEventId()
                                );

                        if (event.getCorrelationId() != null) {
                            message.getMessageProperties()
                                    .setHeader(
                                            "correlationId",
                                            event.getCorrelationId()
                                    );
                        }

                        message.getMessageProperties()
                                .setHeader(
                                        "eventType",
                                        event.getEventType()
                                );

                        return message;
                    }
            );

            event.markPublished();

            outboxRepository.save(event);

        } catch (Exception ex) {

            LocalDateTime nextAttempt =
                    calculateNextAttempt(
                            event.getAttempts()
                    );

            event.markFailed(
                    truncateError(ex.getMessage()),
                    nextAttempt
            );

            outboxRepository.save(event);
        }
    }

    private LocalDateTime calculateNextAttempt(
            int attempts
    ) {

        int delaySeconds;

        if (attempts <= 0) {
            delaySeconds = 2;
        } else if (attempts == 1) {
            delaySeconds = 5;
        } else if (attempts == 2) {
            delaySeconds = 15;
        } else if (attempts == 3) {
            delaySeconds = 30;
        } else {
            delaySeconds = 60;
        }

        return LocalDateTime.now()
                .plusSeconds(delaySeconds);
    }

    private String truncateError(String error) {

        if (error == null) {
            return "Unknown error";
        }

        return error.length() <= 1000
                ? error
                : error.substring(0, 1000);
    }
}