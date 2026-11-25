
package com.example.order.service.repository;

import com.example.order.service.entity.OrderInboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderInboxRepository
        extends JpaRepository<OrderInboxEvent, Long> {

    boolean existsByEventId(Long eventId);
}
