BEGIN;

CREATE TABLE user_account (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(100) NOT NULL UNIQUE CHECK (btrim(name) <> ''),

    password VARCHAR(255) NOT NULL
);

CREATE TABLE todos_list (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,

    list_name VARCHAR(100) NOT NULL CHECK (btrim(list_name) <> ''),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE todos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    list_id BIGINT NOT NULL REFERENCES todos_list(id) ON DELETE CASCADE,

    title VARCHAR(200) NOT NULL CHECK (btrim(title) <> ''),

    description VARCHAR(2000),

    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
    CHECK (
        status IN (
            'NOT_STARTED',
            'IN_PROGRESS',
            'COMPLETED'
        )
    ),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- list_idを検索するための索引を作るSQL (????)
CREATE INDEX idx_todos_list_id ON todos (list_id);

COMMIT;
