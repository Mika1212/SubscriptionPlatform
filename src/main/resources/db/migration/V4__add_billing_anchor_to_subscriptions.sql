ALTER TABLE subscriptions
    ADD COLUMN billing_day_of_month INT;

ALTER TABLE subscriptions
    ADD COLUMN next_billing_date DATE;

UPDATE subscriptions
SET billing_day_of_month = EXTRACT(DAY FROM activation_date)
WHERE billing_day_of_month IS NULL;

UPDATE subscriptions
SET next_billing_date = activation_date
WHERE next_billing_date IS NULL;

ALTER TABLE subscriptions
    ALTER COLUMN billing_day_of_month SET NOT NULL;

ALTER TABLE subscriptions
    ALTER COLUMN next_billing_date SET NOT NULL;

CREATE INDEX idx_subscriptions_billing
    ON subscriptions(status, next_billing_date);
