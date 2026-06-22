# 防災マップ（Bousai Map）

地域住民向けの多言語・防災情報ポータル。避難所マップ、防災 AI 問答、防災イベント情報を提供する。
KCGI 演習活動4 / チーム45（SDG 目標11「住み続けられるまちづくりを」）。

> 開発の詳細な規約・アーキテクチャは [CLAUDE.md](CLAUDE.md) を参照。

## 構成

```
bousai/
├── backend/   Spring Boot 4.1 (Java 21) — REST API / JPA / Flyway / AI
└── frontend/  React + TypeScript + Vite — SPA / i18n(ja,en,zh)
```

## 前提ツール

| ツール | バージョン |
| --- | --- |
| JDK | 21（LTS） |
| Maven | 3.9+ |
| Node.js | 20+（推奨 22/24） |

## 起動方法（開発）

### バックエンド（SQLite, ポート 8080）

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- 起動時に Flyway がマイグレーションを実行し、`backend/data/bousai.db` を自動生成する。
- ヘルスチェック: <http://localhost:8080/actuator/health>

### フロントエンド（ポート 5173）

```bash
cd frontend
npm install
npm run dev
```

- `http://localhost:5173` を開く。
- `/api` へのリクエストは Vite プロキシ経由でバックエンド(8080)に転送される（CORS 不要）。

## 主な API

| メソッド | パス | 説明 |
| --- | --- | --- |
| GET | `/api/v1/shelters` | 避難所一覧 |
| POST | `/api/v1/chat` | 防災 AI 問答（現在はモック実装。`{"question","lang"}`） |
| GET | `/actuator/health` | ヘルスチェック |

## テスト / ビルド

```bash
# バックエンド
cd backend
mvn test            # スモークテスト含む
mvn clean package   # jar 生成

# フロントエンド
cd frontend
npm run build       # 型チェック + 本番ビルド
npm run lint
```

## 注意

- **AI はモック実装**：LLM 未接続。`ChatAssistant` インタフェースの実装を Spring AI（ChatClient + RAG）に差し替え、API キーを環境変数で渡す予定（[CLAUDE.md](CLAUDE.md) §6）。
- **機密情報**（API キー / DB パスワード / JWT シークレット）は環境変数のみ。`*.db` や `.env` はコミット禁止。
- 本番は PostgreSQL（`prod` プロファイル）。マイグレーションは方言別に `db/migration/{vendor}` で管理。
