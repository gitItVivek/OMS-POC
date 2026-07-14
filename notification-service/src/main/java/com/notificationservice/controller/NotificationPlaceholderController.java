package com.notificationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Phase 0 placeholder — real notification sending comes later.
 */
@RestController
@RequestMapping("/internal/notifications")
public class NotificationPlaceholderController {

    private static final Logger log = LoggerFactory.getLogger(NotificationPlaceholderController.class);

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendPlaceholder(@RequestBody Map<String, Object> body) {
        Object orderId = body.get("orderId");
        log.info("NOTIFICATION PLACEHOLDER — orderId={}, message={}", orderId, body.get("message"));
        return ResponseEntity.ok(Map.of(
                "status", "PLACEHOLDER",
                "orderId", orderId == null ? "" : orderId.toString(),
                "message", "Notification not implemented yet"
        ));
    }
}
