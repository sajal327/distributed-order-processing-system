package com.example.payment.service.repository;

import com.example.payment.service.domain.PaymentOutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, Long> {

    @Query("""
        SELECT e FROM PaymentOutboxEvent e
        WHERE e.status = 'PENDING'
        AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
        ORDER BY e.createdAt ASC
        """)
    List<PaymentOutboxEvent> findPendingEvents(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}