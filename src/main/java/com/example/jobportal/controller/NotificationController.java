package com.example.jobportal.controller;

import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.model.Notification;
import com.example.jobportal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    ResponseEntity<ApiResponse<List<Notification>>> getNotifications(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(userId)));
    }

    @PutMapping("/{id}/read")
    ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id, Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PutMapping("/read-all")
    ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @GetMapping("/unread-count")
    ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(userId)));
    }
}
