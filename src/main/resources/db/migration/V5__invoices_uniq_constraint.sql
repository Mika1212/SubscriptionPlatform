ALTER TABLE invoices
    ADD CONSTRAINT uniq_subscription_billing
        UNIQUE (subscription_id, billing_date);
