CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               event_type VARCHAR(50) NOT NULL,
                               payload JSONB NOT NULL,
                               status VARCHAR(10) NOT NULL,
                               created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_outbox_status ON outbox_events(status);
