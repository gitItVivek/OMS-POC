ALTER TABLE saga_instances
    ADD COLUMN IF NOT EXISTS event_adapter VARCHAR(32) NOT NULL DEFAULT 'CAMEL';

COMMENT ON COLUMN saga_instances.event_adapter IS
    'Kafka event adapter that owns this saga: CAMEL (thin Camel) or SPRING_INTEGRATION';
