package com.integrationservice.kafka;

/**
 * Values stored on saga_instances.event_adapter and used by Kafka listeners
 * to decide whether to process an event for that order.
 */
public final class SagaEventAdapters {

    public static final String CAMEL = "CAMEL";
    public static final String SPRING_INTEGRATION = "SPRING_INTEGRATION";

    private SagaEventAdapters() {
    }
}
