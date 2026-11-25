package com.example.payment.service.messaging;

import com.example.payment.service.config.RabbitMQConfig;
import com.example.payment.service.domain.PaymentOutboxEvent;
import com.example.payment.service.repository.PaymentOutboxRepository;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentOutboxPublisher {

    private static final int BATCH_SIZE = 50;

    private final PaymentOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public PaymentOutboxPublisher(
            PaymentOutboxRepository outboxRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<PaymentOutboxEvent> events =
                outboxRepository.findPendingEvents(
                        LocalDateTime.now(),
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (PaymentOutboxEvent event : events) {
            publishEvent(event);
        }
    }

    @Transactional
    public void publishEvent(PaymentOutboxEvent event) {

        try {

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EVENTS_EXCHANGE,
                    event.getRoutingKey(),
                    event.getPayload(),
                    message -> {

                        // Explicitly identify the existing payload as JSON
                        message.getMessageProperties()
                                .setContentType(MessageProperties.CONTENT_TYPE_JSON);

                        message.getMessageProperties()
                                .setHeader("eventId", event.getEventId());

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
                    calculateNextAttempt(event.getAttempts());

            event.markFailed(
                    truncateError(ex.getMessage()),
                    nextAttempt
            );

            outboxRepository.save(event);
        }
    }

    private LocalDateTime calculateNextAttempt(int attempts) {

        long delaySeconds;

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

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    private String truncateError(String error) {

        if (error == null || error.isBlank()) {
            return "Unknown RabbitMQ publishing error";
        }

        return error.length() <= 1000
                ? error
                : error.substring(0, 1000);
    }
}