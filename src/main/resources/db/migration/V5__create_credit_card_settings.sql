CREATE TABLE credit_card_settings
(
    id           UUID PRIMARY KEY,
    account_id   UUID UNIQUE    NOT NULL REFERENCES accounts (id),
    closing_day  INTEGER        NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day      INTEGER        NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    limit_amount DECIMAL(19, 4) NOT NULL,
    is_active    BOOLEAN        NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL,
    deleted_at   TIMESTAMPTZ,
    created_by   UUID           NOT NULL,
    updated_by   UUID           NOT NULL
);
