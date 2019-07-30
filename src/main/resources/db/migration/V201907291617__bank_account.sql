CREATE TABLE bank_account(
    id SERIAL PRIMARY KEY,
    user_uuid UUID REFERENCES app_user(uuid) NOT NULL,
    account VARCHAR(64) NOT NULL,
    format VARCHAR(8) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_bank_account_user_uuid ON bank_account(user_uuid);
