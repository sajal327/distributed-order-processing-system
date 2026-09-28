package com.example.notification.service.service;

import com.example.notification.service.config.RabbitMQConfig;
import com.example.notification.service.domain.Notification;
import com.example.notification.service.domain.NotificationInboxEvent;
import com.example.notification.service.domain.NotificationOutboxEvent;
import com.example.notification.service.messaging.PaymentCompletedEvent;
import com.example.notification.service.repository.NotificationInboxRepository;
import com.example.notification.service.repository.NotificationOutboxRepository;
import com.example.notification.service.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationInboxRepository notificationInboxRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ObjectMapper objectMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationInboxRepository notificationInboxRepository,
            NotificationOutboxRepository notificationOutboxRepository,
            ObjectMapper objectMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationInboxRepository = notificationInboxRepository;
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void processPaymentCompleted(
            PaymentCompletedEvent event
    ) {

        if (notificationInboxRepository
                .existsByEventId(event.eventId())) {
            return;
        }

        validate(event);

        String amount = formatAmount(
                event.amount(),
                event.currency()
        );

        Notification notification =
                new Notification(
                        event.userId(),
                        "PAYMENT_SUCCESS",
                        "Payment Successful",
                        "Your payment of "
                                + amount
                                + " for order #"
                                + event.orderId()
                                + " was successful.",
                        "ORDER",
                        event.orderId(),
                        event.correlationId()
                );

        notificationRepository.save(notification);

        try {
            String payload =
                    objectMapper.writeValueAsString(notification);

            NotificationOutboxEvent outboxEvent =
                    new NotificationOutboxEvent(
                            event.eventId(),
                            "ORDER",
                            event.orderId(),
                            event.correlationId(),
                            "NOTIFICATION_CREATED",
                            "notification.created",
                            payload
                    );

            notificationOutboxRepository.save(outboxEvent);

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Failed to serialize notification",
                    ex
            );
        }

        notificationInboxRepository.save(
                new NotificationInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                )
        );

        System.out.println(
                "NOTIFICATION_SENT userId="
                        + event.userId()
                        + " orderId="
                        + event.orderId()
                        + " type=PAYMENT_SUCCESS"
        );
    }

    private void validate(PaymentCompletedEvent event) {

        if (event.eventId() == null) {
            throw new IllegalArgumentException(
                    "eventId cannot be null"
            );
        }

        if (event.orderId() == null) {
            throw new IllegalArgumentException(
                    "orderId cannot be null"
            );
        }

        if (event.userId() == null) {
            throw new IllegalArgumentException(
                    "userId cannot be null"
            );
        }

        if (event.amount() == null
                || event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "amount must be greater than zero"
            );
        }

        if (event.currency() == null
                || event.currency().isBlank()
                || event.currency().length() != 3) {
            throw new IllegalArgumentException(
                    "currency must be a 3-character code"
            );
        }
    }

    private String formatAmount(
            BigDecimal amount,
            String currency
    ) {

        if ("INR".equalsIgnoreCase(currency)) {
            return "₹" + amount.setScale(2);
        }

        return amount.setScale(2) + " " + currency;
    }
}