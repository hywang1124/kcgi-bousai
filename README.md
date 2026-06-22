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
| GET | `/api/v1/shelters` | 避難所一覧（公開） |
| POST | `/api/v1/chat` | 防災 AI 問答（公開・現在はモック実装。`{"question","lang"}`） |
| POST | `/api/v1/auth/register` | セルフ登録（公開・既定ロール USER。`{"username","password"}`） |
| POST | `/api/v1/auth/login` | ログインして JWT を取得（`{"username","password"}`） |
| POST | `/api/v1/shelters` | 避難所を作成（**ADMIN**） |
| PUT | `/api/v1/shelters/{id}` | 避難所を更新（**ADMIN**） |
| DELETE | `/api/v1/shelters/{id}` | 避難所を削除（**ADMIN**） |
| GET | `/api/v1/admin/users` | ユーザ一覧（**ADMIN**） |
| PUT | `/api/v1/admin/users/{id}/role` | 役割変更（**ADMIN**・`{"role":"ADMIN|EDITOR|USER"}`） |
| GET | `/api/v1/admin/me` | 認証中の管理者情報（要 ADMIN・`Authorization: Bearer <token>`） |
| GET | `/actuator/health` | ヘルスチェック（公開） |

### 認証 / 認可（管理後台）

- ステートレス JWT（HS256）。役割は **ADMIN / EDITOR / USER** の 3 種。
- 公開: 避難所一覧（GET）・AI 問答・登録・ログイン・ヘルス。
- **ADMIN 限定**: 避難所の作成/更新/削除、`/api/v1/admin/**`（ユーザ・役割管理）。
- **dev** 管理者: `admin` / `admin12345`（初回起動時に自動作成）。
- **本番** は秘密鍵を環境変数 `JWT_SECRET`（32 バイト以上）で必ず指定する。

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
