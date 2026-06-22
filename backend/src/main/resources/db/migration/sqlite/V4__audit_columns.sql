-- =========================================================
-- V4 監査カラム追加 (SQLite / dev)
-- created_at は各テーブルに既存。created_by / last_modified_at / last_modified_by を追加。
-- 既存行は system で埋める（NOT NULL は付けない＝既存行の都合上）。
-- =========================================================

-- shelters
ALTER TABLE shelters ADD COLUMN created_by TEXT;
ALTER TABLE shelters ADD COLUMN last_modified_at TIMESTAMP;
ALTER TABLE shelters ADD COLUMN last_modified_by TEXT;

-- chat_logs
ALTER TABLE chat_logs ADD COLUMN created_by TEXT;
ALTER TABLE chat_logs ADD COLUMN last_modified_at TIMESTAMP;
ALTER TABLE chat_logs ADD COLUMN last_modified_by TEXT;

-- users
ALTER TABLE users ADD COLUMN created_by TEXT;
ALTER TABLE users ADD COLUMN last_modified_at TIMESTAMP;
ALTER TABLE users ADD COLUMN last_modified_by TEXT;

-- 既存行のバックフィル
UPDATE shelters  SET last_modified_at = created_at, created_by = 'system', last_modified_by = 'system';
UPDATE chat_logs SET last_modified_at = created_at, created_by = 'system', last_modified_by = 'system';
UPDATE users     SET last_modified_at = created_at, created_by = 'system', last_modified_by = 'system';
