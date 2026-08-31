package com.example.inventory.service.repository;

import com.example.inventory.service.entity.InventoryInboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryInboxRepository
        extends JpaRepository<InventoryInboxEvent, Long> {

    boolean existsByEventId(Long eventId);
}