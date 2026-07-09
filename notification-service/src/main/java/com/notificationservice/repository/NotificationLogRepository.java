package com.notificationservice.repository;

import com.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    @Query("""
            SELECT COUNT(n) FROM NotificationLog n
            WHERE n.customerId = :customerId
              AND n.notificationType = :notificationType
              AND n.status = 'SENT'
              AND n.createdAt >= :since
            """)
    long countSentSince(
            @Param("customerId") UUID customerId,
            @Param("notificationType") String notificationType,
            @Param("since") Instant since);
}
