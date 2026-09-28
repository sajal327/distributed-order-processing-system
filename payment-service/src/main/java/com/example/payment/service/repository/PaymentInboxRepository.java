package com.example.payment.service.repository;

import com.example.payment.service.domain.PaymentInboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentInboxRepository
        extends JpaRepository<PaymentInboxEvent, Long> {

    boolean existsByEventId(Long eventId);
}