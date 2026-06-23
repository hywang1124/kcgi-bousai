# 防災マップ（Bousai Map）

地域住民向けの多言語・防災情報ポータル。避難所マップ・防災知識・防災 AI 問答を、外国人住民や子どもにも分かりやすく多言語（ja / en / zh / zh-TW）で提供する。
KCGI 演習活動4 / チーム45（SDG 目標11「住み続けられるまちづくりを」）。

> 開発の詳細な規約・アーキテクチャは [CLAUDE.md](CLAUDE.md) を参照。

## 構成

```
bousai/
├── backend/   Spring Boot 4.1 (Java 21) — AI 聊天 REST API のみ（DB なし・認証なし）
└── frontend/  React + TypeScript + Vite — SPA（GitHub Pages へデプロイ）
```

- **避難所・防災知識などは前端の静的データ**（バックエンド不要）。
- バックエンドは **AI 問答のみ**。データベースは使わない。

## 前提ツール

| ツール | バージョン |
| --- | --- |
| JDK | 21（LTS） |
| Maven | 3.9+ |
| Node.js | 20+（推奨 22/24） |

## 起動方法（開発）

### バックエンド（AI 聊天 API・ポート 8080）

```bash
cd backend
mvn spring-boot:run
```

- データベース不要。約 2 秒で起動する。
- ヘルスチェック: <http://localhost:8080/actuator/health>

### フロントエンド（ポート 5173）

```bash
cd frontend
npm install
npm run dev
```

- `http://localhost:5173` を開く。
- `/api` へのリクエストは Vite プロキシ経由でバックエンド(8080)へ転送される。

## API

| メソッド | パス | 説明 |
| --- | --- | --- |
| POST | `/api/v1/chat` | 防災 AI 問答（公開・現在はモック実装。`{"question","lang"}`） |
| GET | `/actuator/health` | ヘルスチェック |

## テスト / ビルド

```bash
# バックエンド
cd backend
mvn test            # AI 聊天のスモークテスト
mvn clean package   # jar 生成

# フロントエンド
cd frontend
npm run build       # 型チェック + 本番ビルド
npm run lint
```

## デプロイ

### フロントエンド（GitHub Pages）

- `main` ブランチへ push すると [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) が自動でビルド・公開する。
- リポジトリ設定：**Settings → Pages → Source = GitHub Actions**。
- バックエンドの公開 URL はリポジトリ変数 **`VITE_API_BASE_URL`**（Settings → Secrets and variables → Actions → Variables）で指定する。未設定だと AI 問答だけ動かない（他の静的ページは動作する）。
- `vite.config.ts` は `base: './'` のため、プロジェクトページ（`/<repo>/` 配下）でもそのまま動く。

### バックエンド

- `mvn clean package` で jar を作り `java -jar target/bousai-0.0.1-SNAPSHOT.jar` で起動、または Docker 化して小型クラウド / コンテナ基盤へ。
- 環境変数：
  - `APP_CORS_ALLOWED_ORIGINS` … CORS 許可オリジン（カンマ区切り）。本番は GitHub Pages のドメイン（例 `https://<user>.github.io`）を指定。
  - （将来 LLM 接続時）LLM API key も環境変数で注入する。
- **データベース不要**。

## 注意

- **AI はモック実装**：LLM 未接続。`ChatAssistant` インタフェースの実装を Spring AI（`ChatClient` + RAG）に差し替え、API キーを環境変数で渡す予定（[CLAUDE.md](CLAUDE.md) §6）。
- **避難所データ**は実在の公的データ（国土地理院「指定緊急避難場所データ」京都市）。座標を捏造しない。
- **機密情報**（LLM API キー等）は環境変数のみ。`.env` はコミット禁止。
