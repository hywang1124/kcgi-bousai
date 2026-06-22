-- =========================================================
-- V5 危険区域 hazard_zones (SQLite / dev)
-- 区域几何は GeoJSON 文字列で保持（PostGIS 非依存・双库共通）。
-- 監査カラムは新規テーブルのため最初から含める。
-- =========================================================
CREATE TABLE hazard_zones (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    type             TEXT      NOT NULL,
    severity         TEXT      NOT NULL,
    name_ja          TEXT      NOT NULL,
    name_en          TEXT,
    name_zh          TEXT,
    description      TEXT,
    geojson          TEXT      NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       TEXT,
    last_modified_at TIMESTAMP,
    last_modified_by TEXT
);

-- 開発用サンプル（※実在のハザード情報ではない。dev 動作確認用）
INSERT INTO hazard_zones (type, severity, name_ja, name_en, name_zh, description, geojson, created_by, last_modified_by) VALUES
 ('FLOOD', 'HIGH', 'サンプル浸水想定区域', 'Sample Flood Zone', '样例浸水区域', '大雨時の浸水想定（サンプル）',
  '{"type":"Polygon","coordinates":[[[135.760,35.005],[135.775,35.005],[135.775,35.015],[135.760,35.015],[135.760,35.005]]]}',
  'system', 'system'),
 ('LANDSLIDE', 'MEDIUM', 'サンプル土砂災害警戒区域', 'Sample Landslide Zone', '样例土砂灾害区域', '土砂崩れ警戒（サンプル）',
  '{"type":"Polygon","coordinates":[[[135.778,35.025],[135.788,35.025],[135.788,35.033],[135.778,35.033],[135.778,35.025]]]}',
  'system', 'system');

UPDATE hazard_zones SET last_modified_at = created_at;
