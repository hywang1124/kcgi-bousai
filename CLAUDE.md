# CLAUDE.md — 防災マップ Web System

> 本文件是 Claude Code 的项目记忆文件，每次会话启动时自动加载。
> 维护原则：**简洁、准确、及时更新**。过时的架构描述比没有更糟。建议保持在 200 行以内，过长会降低遵守度。

---

## 1. 项目概述

- **课题背景**：KCGI 演習活動4 / チーム番号 45，对应 SDG **目标11「住み続けられるまちづくりを」**。
- **系统名**：防災マップ（Bousai Map）—— 面向地区居民的防災（防灾）信息门户。
- **核心目标**：把地区的 hazard map（防灾地图）重做成**多语言、对儿童友好**的版本；提供**防灾知识 AI 问答**；管理防灾 workshop / 活动信息。
- **价值主张**：「誰も取り残さない（不让任何人掉队）」—— 多语言 + 易懂 + 可交互，覆盖外国居民与儿童。
- **提交说明**：本文件是开发用工具文件，**不提交到 KING-LMS**；课题提交物（报告/PPT）另行用日语撰写。

---

## 2. 技术栈（Tech Stack）

| 层 | 技术 | 说明 |
| --- | --- | --- |
| 后端 | **Java 21 + Spring Boot 4.1** | 4.1.0（2026-06 发布，支持到 2027-07）|
| Web | spring-boot-starter-web | REST API |
| 数据 | spring-boot-starter-data-jpa + Hibernate | ORM |
| 校验 | spring-boot-starter-validation | DTO 校验 |
| 安全 | spring-boot-starter-security | 管理后台鉴权（JWT） |
| 监控 | spring-boot-starter-actuator | 健康检查 / 指标 |
| 迁移 | Flyway | 数据库版本管理 |
| **AI** | **Spring AI 2.0.x**（`spring-ai-starter-model-openai` + `spring-ai-starter-vector-store-*`） | 防灾 AI 问答（RAG）；2.0 基于 Boot 4 基线 |
| 前端 | **React + TypeScript + Vite** | SPA |
| 地图 | **MapLibre GL JS / Leaflet** + OpenStreetMap | 交互式防灾地图 |
| i18n | react-i18next | 多语言（ja / en / zh / 必要时 ko / vi） |
| 数据库 | **SQLite（开发）** + **PostgreSQL（生产）** | 见 §5 |
| 部署 | **AWS**（ECS Fargate / RDS / S3 / CloudFront） | 见 §9 |
| 构建 | Maven（后端）、npm（前端） | |

> 选型理由不写进本文件正文；如需变更技术栈，先与团队确认再改本表。
>
> **Spring Boot 4 注意（与 3.x 不同，易踩坑）**：
> - **Jackson 3**（包名 `tools.jackson.*`），不再支持 Jackson 2，序列化代码用 Jackson 3 API。
> - **JUnit 5（Jupiter）**，JUnit 4 已完全移除。
> - Undertow 已移除；嵌入式容器用 Tomcat / Jetty。
> - 自动配置已模块化拆分，依赖按需引入。
> - Spring AI 2.0 选项类**只能用 builder/构造器**（setter 已移除，强制不可变）。

---

## 3. 系统架构

分层架构（Layered + RAG 模块），前后端分离：

```
┌─────────────────────────────────────────────────────────┐
│  前端 SPA (React + Vite)                                  │
│  ├─ 地图页 (MapLibre)   ├─ 防灾知识 / 活动页                │
│  ├─ AI 问答聊天框        └─ i18n (ja/en/zh)               │
└───────────────┬─────────────────────────────────────────┘
                │ REST / JSON (HTTPS)
┌───────────────▼─────────────────────────────────────────┐
│  后端 Spring Boot                                         │
│  ┌─ Controller 层  (REST API, DTO 校验)                  │
│  ├─ Service 层     (业务逻辑)                             │
│  ├─ Repository 层  (Spring Data JPA)                     │
│  └─ AI 模块        (Spring AI: ChatClient + RAG)         │
│       ├─ EmbeddingModel  → 向量化防灾文档                  │
│       ├─ VectorStore     → 检索相关上下文                  │
│       └─ ChatClient      → 调用 LLM 生成回答              │
└───────┬───────────────────────────┬─────────────────────┘
        │                           │
┌───────▼─────────┐         ┌───────▼──────────────────────┐
│ 关系数据库       │         │ 向量库                         │
│ SQLite (dev)    │         │ SimpleVectorStore (dev, 文件)  │
│ PostgreSQL(prod)│         │ pgvector @ PostgreSQL (prod)  │
└─────────────────┘         └──────────────────────────────┘
        │
┌───────▼─────────────────────────────────────────────────┐
│ 外部：LLM API (OpenAI 兼容)、OpenStreetMap 瓦片            │
└─────────────────────────────────────────────────────────┘
```

**关键约定**：业务依赖方向严格 `Controller → Service → Repository`，禁止 Controller 直接调 Repository；Entity 不出 Service 层，对外一律用 DTO。

---

## 4. 核心特性（Features）

1. **交互式防灾地图**：展示避难所（避難所）、危险区域（浸水/土砂崩れ）、应急设施；点击 marker 看详情；支持当前位置定位与最近避难所路线。
2. **AI 防灾问答（必含，基于 Spring AI + RAG）**：用户用自然语言提问（「最寄りの避難所は？」「地震が来たらどうする？」），系统检索本地防灾文档生成回答，**按用户语言回答**，并附引用来源。详见 §6。
3. **多语言支持**：ja / en / zh（含儿童易懂版文案），UI 与内容均 i18n。
4. **防灾知识库**：分类的防灾手册、各灾种应对、儿童版图解。
5. **活动 / Workshop 管理**：小学防灾 workshop 的日程发布与报名信息展示。
6. **管理后台**：内容管理（地图点位、知识文档、活动），Spring Security + JWT 鉴权。

---

## 5. 数据库设计（双数据库支持）

**策略：用 Spring Profile 切换，统一走 JPA 抽象。**

- `dev` profile → **SQLite**（零配置、文件库，便于本地开发与队员协作）。
- `prod` profile → **PostgreSQL**（生产，且 pgvector 扩展同时承载向量库）。

```properties
# application-dev.properties
spring.datasource.url=jdbc:sqlite:./data/bousai.db
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect

# application-prod.properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:5432/bousai
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

**主要表**：
- `shelters`（避难所：name, lat, lng, capacity, facilities, multilingual fields）
- `hazard_zones`（危险区域：type, geo 多边形/范围, severity）
- `disaster_docs`（防灾文档：title, category, content, lang —— 同时是 RAG 语料源）
- `events`（workshop：title, datetime, location, capacity）
- `users`（管理员：username, password_hash, role）
- `chat_logs`（AI 问答记录：question, answer, lang, sources, created_at）

**重要约束**：
- Flyway 迁移脚本要兼顾 SQLite 与 PostgreSQL 的方言差异（如自增、布尔、JSON 类型）。**避免使用某一方独有的语法**；必要时按 profile 分目录放置迁移脚本。
- 地理字段开发期用 `lat`/`lng` 双 `DOUBLE` 列即可，**不要**在 SQLite 上依赖 PostGIS。
- 所有 schema 变更**必须**通过 Flyway，禁止用 `ddl-auto=update` 改生产结构（dev 可临时用 `validate`/`none`）。

---

## 6. AI 模块规范（Spring AI）

- 通过 **`ChatClient`** 统一调用，**不要**直接 new 各家 SDK；模型 provider 用配置切换，便于换 OpenAI / 本地兼容模型。
- 采用 **RAG**：`disaster_docs` 经 `EmbeddingModel` 向量化写入 `VectorStore`；提问时检索 Top-K 相关文档注入上下文。**推荐用 2.0 的 Advisors API（`RetrievalAugmentationAdvisor`）**接入 RAG，而非手动拼 prompt。
  - dev：`SimpleVectorStore`（文件持久化，无需额外服务）。
  - prod：`PgVectorStore`（复用 PostgreSQL）。
- **系统 prompt 要求**：仅基于检索到的防灾资料回答；信息不足时明确说「資料にありません」并引导联系当地自治体，**禁止编造避难所位置、电话、灾害指引**（安全关键，幻觉可能危及人身安全）。
- **多语言**：检测/接收用户语言参数，用对应语言作答。
- API key 等机密只走环境变量（见 §8），**绝不**写入代码或提交到 git。
- 每次问答落库 `chat_logs`（含 sources）以便复查与改进。

---

## 7. 项目结构

```
bousai-map/
├── backend/                      # Spring Boot
│   ├── src/main/java/jp/kcgi/bousai/
│   │   ├── controller/           # REST API
│   │   ├── service/              # 业务逻辑
│   │   ├── repository/           # Spring Data JPA
│   │   ├── domain/               # Entity
│   │   ├── dto/                  # 请求/响应 DTO
│   │   ├── ai/                   # Spring AI: ChatClient 封装、RAG
│   │   ├── config/               # 配置类（Security、CORS、AI）
│   │   └── BousaiApplication.java
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties
│   │   ├── application-prod.properties
│   │   └── db/migration/         # Flyway
│   └── pom.xml
├── frontend/                     # React + Vite
│   ├── src/{components,pages,api,i18n,locales}/
│   └── package.json
└── CLAUDE.md
```

---

## 8. 开发规范

**Java / Spring**
- 包名 `jp.kcgi.bousai.*`；类名 PascalCase，方法/变量 camelCase，常量 UPPER_SNAKE。
- Controller 只做参数校验与编排；业务进 Service；DB 访问只在 Repository。
- 对外一律返回 DTO，**禁止**直接暴露 Entity。
- 统一异常处理用 `@RestControllerAdvice`；REST 用规范 HTTP 状态码。
- 构造器注入（配 `final` 字段），**不用**字段注入 `@Autowired`。
- 注释 / Javadoc 用**日语**（与课题语言一致，方便老师评审与队内协作）。

**API**
- 路径 `/api/v1/...`，资源名复数：`/api/v1/shelters`、`/api/v1/chat`。
- 请求/响应 JSON 字段 camelCase。

**前端**
- TypeScript 严格模式；组件函数式 + Hooks。
- 所有用户可见文案走 i18n，**禁止**硬编码语言文本。

**安全 / 机密**
- 机密（LLM API key、DB 密码、JWT secret）只用**环境变量**，本地放 `.env`（已 gitignore）。
- **禁止**把任何密钥、`*.db`、`node_modules/`、`target/` 提交到 git。

**Git**
- 分支：`main`（稳定）/ `feature/xxx`。提交信息用祈使句，能说明改了什么。

---

## 9. 部署（AWS）

- 容器化：后端打 jar → Docker 镜像；前端 `npm run build` 出静态文件。
- **前端**：S3 + CloudFront（静态托管 + CDN）。
- **后端**：ECS Fargate（或单机 EC2，按预算）。
- **数据库**：RDS for PostgreSQL（启用 pgvector）。
- **机密**：AWS Secrets Manager / SSM Parameter Store，注入容器环境变量。
- 生产启动加 `--spring.profiles.active=prod`。
- 预算有限时的最简方案：单台 EC2 跑后端 + Nginx 托管前端 + RDS（或同机 PostgreSQL）。

---

## 10. 常用命令

```bash
# 后端（在 backend/）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # 本地运行(SQLite)
./mvnw test                                             # 测试
./mvnw clean package                                    # 打 jar

# 前端（在 frontend/）
npm install
npm run dev        # 开发服务器
npm run build      # 生产构建
npm run lint       # 代码检查

# 数据库迁移
./mvnw flyway:migrate -Dspring-boot.run.profiles=dev
```

---

## 11. 红线（Don'ts）

- ❌ 不在 AI 回答中编造避难所位置/电话/灾害指引（安全关键）。
- ❌ 不提交任何密钥、`.env`、`*.db` 到 git。
- ❌ 不用 `ddl-auto=update` 改生产库结构，schema 一律走 Flyway。
- ❌ Controller 不直接访问 Repository；Entity 不出 Service 层。
- ❌ 不写只在 SQLite 或只在 PostgreSQL 能跑的 SQL（保持双库兼容）。
- ❌ 前端不硬编码可见文案（必须 i18n）。
