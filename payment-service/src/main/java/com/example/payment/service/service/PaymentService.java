package com.example.payment.service.service;

import com.example.payment.service.config.RabbitMQConfig;
import com.example.payment.service.domain.Payment;
import com.example.payment.service.domain.PaymentInboxEvent;
import com.example.payment.service.domain.PaymentOutboxEvent;
import com.example.payment.service.domain.PaymentStatus;
import com.example.payment.service.messaging.InventoryFailedEvent;
import com.example.payment.service.messaging.InventoryReservedEvent;
import com.example.payment.service.messaging.PaymentCompletedEvent;
import com.example.payment.service.repository.PaymentInboxRepository;
import com.example.payment.service.repository.PaymentOutboxRepository;
import com.example.payment.service.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentInboxRepository paymentInboxRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentInboxRepository paymentInboxRepository,
            PaymentOutboxRepository paymentOutboxRepository,
            ObjectMapper objectMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentInboxRepository = paymentInboxRepository;
        this.paymentOutboxRepository = paymentOutboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Processes a successful inventory reservation.
     *
     * Transaction boundary:
     *
     * 1. Check inbox for duplicate event
     * 2. Create/update payment
     * 3. Create payment.completed outbox event
     * 4. Record inbox event
     * 5. Commit everything together
     */
    @Transactional(rollbackFor = Exception.class)
    public void processInventoryReserved(
            InventoryReservedEvent event
    ) {

        // Idempotency check
        if (paymentInboxRepository.existsByEventId(event.eventId())) {
            return;
        }

        validatePaymentData(event);

        /*
         * Find existing payment for this order.
         *
         * The unique constraint on order_id also protects
         * against multiple payments being created for one order.
         */
        Payment payment =
                paymentRepository
                        .findByOrderId(event.orderId())
                        .orElse(null);

        if (payment == null) {

            payment = new Payment(
                    event.orderId(),
                    event.userId(),
                    event.amount(),
                    event.currency(),
                    PaymentStatus.SUCCEEDED,
                    event.correlationId()
            );

        } else {

            /*
             * If a payment already exists, don't create another one.
             *
             * This is useful if the same business operation is
             * encountered again.
             */
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {

                paymentInboxRepository.save(
                        new PaymentInboxEvent(
                                event.eventId(),
                                event.eventType(),
                                event.orderId(),
                                event.correlationId()
                        )
                );

                return;
            }

            payment.setStatus(PaymentStatus.SUCCEEDED);
        }

        paymentRepository.save(payment);

        /*
         * Payment is currently simulated as successful.
         *
         * Later, this is the place where an actual payment provider
         * such as Razorpay/Stripe/etc. can be integrated.
         */
        PaymentCompletedEvent completedEvent =
                new PaymentCompletedEvent(
                        event.eventId(),
                        "PAYMENT_COMPLETED",
                        event.orderId(),
                        event.userId(),
                        event.amount(),
                        event.currency(),
                        LocalDateTime.now(),
                        event.correlationId(),
                        event.eventId()
                );

        saveOutboxEvent(
                event,
                completedEvent
        );

        /*
         * Record the incoming event only after the payment and
         * outgoing event have been prepared.
         *
         * Because everything is inside the same transaction,
         * a failure anywhere causes the entire transaction to roll back.
         */
        paymentInboxRepository.save(
                new PaymentInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                )
        );
    }

    /**
     * Inventory reservation failed.
     *
     * No payment should be created because the order's inventory
     * was never successfully reserved.
     */
    @Transactional(rollbackFor = Exception.class)
    public void processInventoryFailed(
            InventoryFailedEvent event
    ) {

        // Idempotency check
        if (paymentInboxRepository.existsByEventId(event.eventId())) {
            return;
        }

        paymentInboxRepository.save(
                new PaymentInboxEvent(
                        event.eventId(),
                        event.eventType(),
                        event.orderId(),
                        event.correlationId()
                )
        );
    }

    private void validatePaymentData(
            InventoryReservedEvent event
    ) {

        if (event.orderId() == null) {
            throw new IllegalArgumentException(
                    "Order ID cannot be null"
            );
        }

        if (event.userId() == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        if (event.amount() == null) {
            throw new IllegalArgumentException(
                    "Payment amount cannot be null"
            );
        }

        if (event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (event.currency() == null ||
                event.currency().isBlank()) {

            throw new IllegalArgumentException(
                    "Payment currency cannot be null or blank"
            );
        }

        if (event.currency().length() != 3) {
            throw new IllegalArgumentException(
                    "Payment currency must be a 3-letter ISO currency code"
            );
        }
    }

    private void saveOutboxEvent(
            InventoryReservedEvent sourceEvent,
            PaymentCompletedEvent completedEvent
    ) {

        try {

            String payload =
                    objectMapper.writeValueAsString(
                            completedEvent
                    );

            PaymentOutboxEvent outboxEvent =
                    new PaymentOutboxEvent(
                            sourceEvent.eventId(),
                            "ORDER",
                            sourceEvent.orderId(),
                            sourceEvent.correlationId(),
                            "PAYMENT_COMPLETED",
                            RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY,
                            payload
                    );

            paymentOutboxRepository.save(outboxEvent);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Failed to serialize payment outbox event",
                    ex
            );
        }
    }
}