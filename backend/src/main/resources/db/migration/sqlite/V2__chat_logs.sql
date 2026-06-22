-- =========================================================
-- V2 AI 問答ログ (SQLite / dev)
-- =========================================================
CREATE TABLE chat_logs (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    question   TEXT      NOT NULL,
    answer     TEXT      NOT NULL,
    lang       TEXT      NOT NULL,
    sources    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
