package com.example.inventory.service.repository;

import com.example.inventory.service.entity.InventoryOutboxEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryOutboxRepository
        extends JpaRepository<InventoryOutboxEvent, Long> {

    Optional<InventoryOutboxEvent> findByEventId(
            Long eventId
    );

    @Query("""
        SELECT e
        FROM InventoryOutboxEvent e
        WHERE e.status = 'PENDING'
          AND (
                e.nextAttemptAt IS NULL
                OR e.nextAttemptAt <= :now
          )
        ORDER BY e.id
        """)
    List<InventoryOutboxEvent> findPendingEvents(
            @Param("now") LocalDateTime now,
            PageRequest pageable
    );
}

//package com.example.inventory.service.repository;
//
//import com.example.inventory.service.entity.InventoryOutboxEvent;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//public interface InventoryOutboxRepository
//        extends JpaRepository<InventoryOutboxEvent, Long> {
//
//    Optional<InventoryOutboxEvent> findByEventId(Long eventId);
//
//    @Query("""
//        SELECT e
//        FROM InventoryOutboxEvent e
//        WHERE e.status = 'PENDING'
//          AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
//        ORDER BY e.id
//    """)
//    List<InventoryOutboxEvent> findPendingEvents(
//            LocalDateTime now,
//            PageRequest pageable
//    );
//}