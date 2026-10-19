
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) NOT NULL,
    username VARCHAR(30) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    display_name VARCHAR(80) NOT NULL,
    bio VARCHAR(300),
    profile_photo_url TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email UNIQUE (email),

    CONSTRAINT ck_users_email_normalized
        CHECK (email = LOWER(BTRIM(email))),

    CONSTRAINT ck_users_username_format
        CHECK (username ~ '^[A-Za-z0-9_]{3,30}$')
);

CREATE UNIQUE INDEX uq_users_username_lower
    ON users (LOWER(username));
