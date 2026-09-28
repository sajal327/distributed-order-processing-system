package com.example.notification.service.controller;

import com.example.notification.service.domain.Notification;
import com.example.notification.service.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository =
                notificationRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 20;
        }

        if (size > 100) {
            size = 100;
        }

        PageRequest pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return ResponseEntity.ok(
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                userId,
                                pageable
                        )
        );
    }
}