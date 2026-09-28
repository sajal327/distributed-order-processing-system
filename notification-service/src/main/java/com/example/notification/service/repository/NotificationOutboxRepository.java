package com.example.notification.service.repository;

import com.example.notification.service.domain.NotificationOutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationOutboxRepository
        extends JpaRepository<NotificationOutboxEvent, Long> {

    @Query("""
        SELECT e
        FROM NotificationOutboxEvent e
        WHERE e.status = 'PENDING'
        AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
        ORDER BY e.createdAt ASC
        """)
    List<NotificationOutboxEvent> findPendingEvents(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}