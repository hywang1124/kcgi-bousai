-- =========================================================
-- V1 初期スキーマ (SQLite / dev)
-- 方言差: 自動採番は SQLite では INTEGER PRIMARY KEY AUTOINCREMENT。
--         PostgreSQL 版は db/migration/postgresql/V1__init.sql を参照。
-- =========================================================

-- 避難所
CREATE TABLE shelters (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name_ja     TEXT             NOT NULL,
    name_en     TEXT,
    name_zh     TEXT,
    address     TEXT,
    lat         DOUBLE PRECISION NOT NULL,
    lng         DOUBLE PRECISION NOT NULL,
    capacity    INTEGER,
    facilities  TEXT,
    created_at  TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 開発用サンプルデータ（※実在の避難所情報ではない。dev 環境の動作確認用）
INSERT INTO shelters (name_ja, name_en, name_zh, address, lat, lng, capacity, facilities) VALUES
 ('サンプル第一小学校 避難所', 'Sample No.1 Elementary School Shelter', '样例第一小学避难所', '京都市中京区サンプル町1-1', 35.01160, 135.76810, 300, '飲料水,毛布,AED'),
 ('サンプル中央公民館 避難所', 'Sample Central Community Hall Shelter', '样例中央公民馆避难所', '京都市下京区サンプル町2-2', 34.98530, 135.75850, 200, '飲料水,簡易トイレ'),
 ('サンプル総合体育館 避難所', 'Sample Gymnasium Shelter', '样例综合体育馆避难所', '京都市左京区サンプル町3-3', 35.02670, 135.78230, 500, '飲料水,毛布,AED,医療スペース');
