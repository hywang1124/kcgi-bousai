-- =========================================================
-- V3 管理者ユーザ (SQLite / dev)
-- boolean は SQLite では INTEGER(0/1)。
-- =========================================================
CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT      NOT NULL UNIQUE,
    password_hash TEXT      NOT NULL,
    role          TEXT      NOT NULL,
    enabled       INTEGER   NOT NULL DEFAULT 1,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
