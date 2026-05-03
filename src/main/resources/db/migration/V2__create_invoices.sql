CREATE TABLE invoices (
                          id UUID PRIMARY KEY,
                          user_id UUID NOT NULL,
                          subscription_id UUID NOT NULL,
                          subscription_type VARCHAR(10) NOT NULL,
                          amount INT NOT NULL,
                          billing_date DATE NOT NULL,
                          created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_invoices_user_id ON invoices(user_id);
CREATE UNIQUE INDEX uniq_invoice ON invoices(user_id, billing_date);
