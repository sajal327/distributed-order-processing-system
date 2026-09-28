package com.example.notification.service.repository;

import com.example.notification.service.domain.NotificationInboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationInboxRepository
        extends JpaRepository<NotificationInboxEvent, Long> {

    boolean existsByEventId(Long eventId);
}