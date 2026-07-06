ALTER TABLE saga_instances
    ADD COLUMN IF NOT EXISTS order_id UUID,
    ADD COLUMN IF NOT EXISTS customer_id UUID,
    ADD COLUMN IF NOT EXISTS payload TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_saga_instances_order_id ON saga_instances (order_id);
