CREATE TABLE subscriptions (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               type VARCHAR(10) NOT NULL,
                               status VARCHAR(10) NOT NULL,
                               activation_date DATE NOT NULL,
                               deactivation_date DATE,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE UNIQUE INDEX uniq_active_subscription
    ON subscriptions(user_id)
    WHERE status = 'ACTIVE';
