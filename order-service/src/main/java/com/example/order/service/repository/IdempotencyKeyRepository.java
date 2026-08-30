package com.example.order.service.repository;

import com.example.order.service.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );

    boolean existsByUserIdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );
}