CREATE TABLE forgot_password_token(
    id SERIAL PRIMARY KEY,
    user_uuid UUID REFERENCES app_user(uuid) NOT NULL,
    token UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_forgot_password_token_token ON forgot_password_token(token);
