package com.integrationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineRun {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "orchestration", nullable = false, length = 50)
    private String orchestration;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "elapsed_ms")
    private Long elapsedMs;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "payload")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
