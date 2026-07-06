package com.integrationservice.bench;

import com.integrationservice.client.OrderStatusClient;
import com.integrationservice.entity.PipelineRun;
import com.integrationservice.entity.SagaInstance;
import com.integrationservice.enums.SagaStatus;
import com.integrationservice.repository.PipelineRunRepository;
import com.integrationservice.repository.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BenchOrderStatusService {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final OrderStatusClient orderStatusClient;

    @Transactional(readOnly = true)
    public BenchOrderStatusResponse getStatus(UUID orderId) {
        String orderStatus = orderStatusClient.getOrderStatus(orderId);

        return sagaInstanceRepository.findByOrderId(orderId)
                .map(saga -> buildSagaStatus(orderId, saga, orderStatus))
                .orElseGet(() -> pipelineRunRepository.findByOrderId(orderId)
                        .map(run -> buildPipelineStatus(orderId, run, orderStatus))
                        .orElse(BenchOrderStatusResponse.builder()
                                .orderId(orderId)
                                .orderStatus(orderStatus)
                                .orchestration("UNKNOWN")
                                .build()));
    }

    private BenchOrderStatusResponse buildSagaStatus(UUID orderId, SagaInstance saga, String orderStatus) {
        Long elapsed = saga.getCreatedAt() != null && saga.getStatus() == SagaStatus.COMPLETED
                ? java.time.Duration.between(saga.getCreatedAt(), saga.getUpdatedAt()).toMillis()
                : null;
        return BenchOrderStatusResponse.builder()
                .orderId(orderId)
                .orchestration("SAGA")
                .sagaStatus(saga.getStatus().name())
                .orderStatus(orderStatus)
                .elapsedMs(elapsed)
                .build();
    }

    private BenchOrderStatusResponse buildPipelineStatus(UUID orderId, PipelineRun run, String orderStatus) {
        return BenchOrderStatusResponse.builder()
                .orderId(orderId)
                .orchestration(run.getOrchestration())
                .sagaStatus(run.getStatus())
                .orderStatus(orderStatus)
                .elapsedMs(run.getElapsedMs())
                .build();
    }
}
