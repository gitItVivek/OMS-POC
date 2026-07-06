package com.orderservice.config;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class StockLookupIntegrationConfigTest {

    private final StockLookupIntegrationConfig config = new StockLookupIntegrationConfig();

    @Test
    void stockLookupExecutor_isAFixedThreadPoolOfSizeEight() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) config.stockLookupExecutor();
        try {
            assertThat(executor.getCorePoolSize()).isEqualTo(8);
            assertThat(executor.getMaximumPoolSize()).isEqualTo(8);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void stockLookupExecutor_executesSubmittedTasksConcurrently() throws InterruptedException {
        Executor executor = config.stockLookupExecutor();
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        try {
            int taskCount = 5;
            CountDownLatch latch = new CountDownLatch(taskCount);
            AtomicInteger executedCount = new AtomicInteger();

            for (int i = 0; i < taskCount; i++) {
                executor.execute(() -> {
                    executedCount.incrementAndGet();
                    latch.countDown();
                });
            }

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(executedCount.get()).isEqualTo(taskCount);
        } finally {
            threadPoolExecutor.shutdownNow();
        }
    }

    @Test
    void stockLookupExecutor_returnsANewInstanceOnEachInvocation() {
        ThreadPoolExecutor first = (ThreadPoolExecutor) config.stockLookupExecutor();
        ThreadPoolExecutor second = (ThreadPoolExecutor) config.stockLookupExecutor();
        try {
            assertThat(first).isNotSameAs(second);
        } finally {
            first.shutdownNow();
            second.shutdownNow();
        }
    }
}