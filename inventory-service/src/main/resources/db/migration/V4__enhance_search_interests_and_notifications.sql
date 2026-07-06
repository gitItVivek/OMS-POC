-- Search interest dedup key + notification support for interest alerts (non-order)

ALTER TABLE search_interests ADD COLUMN IF NOT EXISTS interest_key VARCHAR(255);
ALTER TABLE search_interests ADD COLUMN IF NOT EXISTS product_title VARCHAR(500);

UPDATE search_interests
SET interest_key = LOWER(TRIM(search_query))
WHERE interest_key IS NULL AND search_query IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_search_interests_customer_interest_key
    ON search_interests (customer_id, interest_key)
    WHERE interest_key IS NOT NULL;

ALTER TABLE notification_log ALTER COLUMN order_id DROP NOT NULL;

ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS customer_id UUID;
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS notification_type VARCHAR(100);
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS recipient_email VARCHAR(255);
ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS interest_id UUID;

CREATE INDEX IF NOT EXISTS idx_notification_log_customer_type_created
    ON notification_log (customer_id, notification_type, created_at DESC);
