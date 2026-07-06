package com.integrationservice.repository;

import com.integrationservice.entity.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, UUID> {

    Optional<PipelineRun> findByOrderId(UUID orderId);
}
