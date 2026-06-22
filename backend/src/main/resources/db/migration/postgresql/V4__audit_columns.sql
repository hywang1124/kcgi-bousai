-- =========================================================
-- V4 監査カラム追加 (PostgreSQL / prod)
-- created_at は各テーブルに既存。created_by / last_modified_at / last_modified_by を追加。
-- =========================================================

ALTER TABLE shelters
    ADD COLUMN created_by       VARCHAR(64),
    ADD COLUMN last_modified_at TIMESTAMP,
    ADD COLUMN last_modified_by VARCHAR(64);

ALTER TABLE chat_logs
    ADD COLUMN created_by       VARCHAR(64),
    ADD COLUMN last_modified_at TIMESTAMP,
    ADD COLUMN last_modified_by VARCHAR(64);

ALTER TABLE users
    ADD COLUMN created_by       VARCHAR(64),
    ADD COLUMN last_modified_at TIMESTAMP,
    ADD COLUMN last_modified_by VARCHAR(64);

-- 既存行のバックフィル
UPDATE shelters  SET last_modified_at = created_at, created_by = 'system', last_modified_by = 'system';
UPDATE chat_logs SET last_modified_at = created_at, created_by = 'system', last_modified_by = 'system';
UPDATE users     SET last_modified_at = created_at, created_by = 'system', last_modified_by = 'system';
