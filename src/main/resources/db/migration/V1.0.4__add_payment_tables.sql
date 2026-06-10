-- Payment Options: Owner-defined payment types per asset
CREATE TABLE payment_options (
    id          BIGSERIAL PRIMARY KEY,
    asset_id    BIGINT NOT NULL REFERENCES asset(id) ON DELETE CASCADE,
    owner_id    BIGINT NOT NULL REFERENCES users(id),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    amount      NUMERIC(10, 2) NOT NULL,
    currency    VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_type VARCHAR(50) NOT NULL DEFAULT 'RENT',
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_interval VARCHAR(20),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ DEFAULT now()
);

-- Payments: Transaction records
CREATE TABLE payments (
    id                          BIGSERIAL PRIMARY KEY,
    payment_option_id           BIGINT NOT NULL REFERENCES payment_options(id),
    asset_id                    BIGINT NOT NULL REFERENCES asset(id),
    tenant_user_id              BIGINT NOT NULL REFERENCES users(id),
    owner_user_id               BIGINT NOT NULL REFERENCES users(id),
    amount                      NUMERIC(10, 2) NOT NULL,
    currency                    VARCHAR(3) NOT NULL DEFAULT 'USD',
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    stripe_checkout_session_id  VARCHAR(500),
    stripe_payment_intent_id    VARCHAR(500),
    description                 TEXT,
    paid_at                     TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_payment_options_asset_id ON payment_options(asset_id);
CREATE INDEX idx_payment_options_owner_id ON payment_options(owner_id);
CREATE INDEX idx_payments_tenant_user_id ON payments(tenant_user_id);
CREATE INDEX idx_payments_owner_user_id ON payments(owner_user_id);
CREATE INDEX idx_payments_asset_id ON payments(asset_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_stripe_session ON payments(stripe_checkout_session_id);
