# CLAUDE.md — 防災マップ Web System

> 本文件是 Claude Code 的项目记忆文件，每次会话启动时自动加载。
> 维护原则：**简洁、准确、及时更新**。过时的架构描述比没有更糟。建议保持在 200 行以内。

---

## 1. 项目概述

- **课题背景**：KCGI 演習活動4 / チーム番号 45，对应 SDG **目标11「住み続けられるまちづくりを」**。
- **系统名**：防災マップ（Bousai Map）—— 面向地区居民的防災（防灾）信息门户。
- **核心目标**：多语言、对儿童友好的防灾信息门户 + **防灾知识 AI 问答**。
- **价值主张**：「誰も取り残さない（不让任何人掉队）」—— 多语言 + 易懂 + 可交互，覆盖外国居民与儿童。
- **提交说明**：本文件是开发用工具文件，**不提交到 KING-LMS**；课题提交物（报告/PPT）另行用日语撰写。

---

## 2. 技术栈（Tech Stack）

| 层 | 技术 | 说明 |
| --- | --- | --- |
| 后端 | **Java 21 + Spring Boot 4.1** | **仅提供 AI 聊天 REST API** |
| Web | spring-boot-starter-webmvc | REST API |
| 校验 | spring-boot-starter-validation | DTO 校验 |
| 监控 | spring-boot-starter-actuator | 健康检查 |
| **AI** | **Spring AI 2.0.x**（`spring-ai-starter-model-anthropic`） | 防灾 AI 问答（RAG）；LLM 为 **Claude**（`claude-opus-4-8`）。`ANTHROPIC_API_KEY` 未设置时自动回退到 Mock 实现 |
| 前端 | **React + TypeScript + Vite** | SPA，部署到 GitHub Pages |
| 地图 | **MapLibre GL JS** + OpenStreetMap | 展示避难所静态点位 |
| i18n | react-i18next | 多语言（ja / en / zh / zh-TW） |
| **数据库** | **无** | AI 聊天不需要持久化（见 §5）|
| 部署 | 前端 **GitHub Pages**；后端 容器 / 小型云主机 | 见 §9 |
| 构建 | Maven（后端）、npm（前端） | |

> **Spring Boot 4 注意（与 3.x 不同，易踩坑）**：
> - **Jackson 3**（包名 `tools.jackson.*`），序列化用 Jackson 3 API。
> - **JUnit 5（Jupiter）**；Undertow 已移除（用 Tomcat）。
> - 自动配置已模块化拆分，依赖按需引入（如 `spring-boot-starter-webmvc`）。
> - Spring AI 2.0 选项类**只能用 builder/构造器**（setter 已移除）。

---

## 3. 系统架构

前后端分离。**后端只做 AI 聊天，无数据库、无鉴权**；前端是纯静态 SPA（含避难所/知识等静态内容）。

```
┌─────────────────────────────────────────────────────────┐
│  前端 SPA (React + Vite)  —— GitHub Pages 静态托管        │
│  ├─ ホーム / 防災知識 (静态文案)                           │
│  ├─ 避難所・地図 (MapLibre + 静态避難所数据)               │
│  ├─ AI 問答聊天框  └─ i18n (ja/en/zh/zh-TW)              │
└───────────────┬─────────────────────────────────────────┘
                │ REST / JSON (HTTPS, 仅 /api/v1/chat)
┌───────────────▼─────────────────────────────────────────┐
│  后端 Spring Boot（仅 AI 聊天）                           │
│  ┌─ Controller 层 (ChatController, DTO 校验)             │
│  ├─ Service 层    (ChatService)                          │
│  └─ AI 模块       (ChatAssistant: ChatClient + RAG)      │
│       └─ SimpleVectorStore (文件, 无需数据库)             │
└───────────────┬─────────────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────────────┐
│ 外部：Claude API (Anthropic)                              │
└─────────────────────────────────────────────────────────┘
```

**关键约定**：依赖方向 `Controller → Service → AI`；对外一律 DTO。**无数据库 → 无 Repository / Entity**。

---

## 4. 核心特性（Features）

1. **AI 防灾问答（核心后端功能，Spring AI + Claude + RAG）**：自然语言提问，检索本地防灾文档生成回答，**按用户语言回答**并附来源；支持非流式（`/api/v1/chat`）与流式 SSE（`/api/v1/chat/stream`）。`ANTHROPIC_API_KEY` 未设置时回退 Mock 实现（无 key 也可跑通链路），详见 §6。RAG 向量库目前**未导入语料**（基础设施已就位）。
2. **避难所地图（纯前端静态数据）**：数据源为**国土地理院「指定緊急避難場所データ」（京都市）**，编译进前端；MapLibre 展示点位，支持按最近距离排序与地理定位。
3. **防灾知识（纯前端静态）**：地震 / 海啸 / 火山 / 台风 / 洪水 的介绍与备灾要点。
4. **多语言**：ja / en / zh / zh-TW，UI 与内容全部 i18n。

---

## 5. 数据持久化

- **本系统不使用数据库**
- AI 聊天**不落库**（不保存聊天记录）；如将来确需持久化再单独评估。
- 避难所、防灾知识等内容是**前端静态数据**（`frontend/src/data/`、locales），随构建打包。
- RAG 向量库用 Spring AI 的 **`SimpleVectorStore`（文件持久化，无需数据库服务）**。

---

## 6. AI 模块规范（Spring AI）

- 业务只依赖自定义 **`ChatAssistant` 接口**（`generate` 非流式 / `generateStream` 流式 `Flux<String>`）。两个实现由 `AiAssistantConfig` 按 `ANTHROPIC_API_KEY` 是否设置二选一注入，**不改 Controller/Service**：
  - **`SpringAiChatAssistant`**：Spring AI `ChatClient` + Claude（`claude-opus-4-8`）+ `RetrievalAugmentationAdvisor`（RAG）。
  - **`MockChatAssistant`**：无 key 时的回退，模拟流式输出，保证链路始终可跑通。
- **RAG**：`VectorStoreDocumentRetriever` + `SimpleVectorStore`（文件持久化 `backend/data/vector-store.json`，gitignore 对象）。语料来自内閣府防災情報・首相官邸防災ページ・国土交通省「川の防災情報」・気象庁多言語ページ・東京都防災ホームページ等官方网站，存于 `backend/src/main/resources/rag-corpus/*.md`（各文件首行注明出典 URL），由 `RagCorpusLoader` 在启动时切分・向量化并写入持久化文件（已存在则直接加载，不重新嵌入）。
- **嵌入模型**：`TransformersEmbeddingModel`（本地 ONNX，无需 API key），使用多语言模型 `Xenova/paraphrase-multilingual-MiniLM-L12-v2`（在 `AiAssistantConfig` 中指定 `modelResource`/`tokenizerResource`）。**不要改回默认的 `all-MiniLM-L6-v2`**——该模型对日语语义相似度过低（实测 0.25–0.46），会导致 `SpringAiChatAssistant` 中配置的 `similarityThreshold(0.5)` 永远检索不到任何文档。
- **流式**：`POST /api/v1/chat/stream` 返回 `text/event-stream`（Spring MVC 对 `Flux<String>` 自动按 `data:<chunk>\n\n` 分帧）。前端用 `fetch` + `ReadableStream` 解析。
- **系统 prompt 红线**：仅基于检索到的防灾资料回答；信息不足时明确说「資料にありません」并引导联系当地自治体，**禁止编造避难所位置、电话、灾害指引**（安全关键）。
- **多语言**：接收用户语言参数，用对应语言作答。
- API key（`ANTHROPIC_API_KEY`）等机密只走环境变量（见 §8），**绝不**写入代码或提交到 git。
- **模型选择**：除非用户明确要求，新增/调整 LLM 调用一律使用 `claude-opus-4-8`；不要发送 `temperature`/`top_p`/`top_k`（该模型会拒绝）。

---

## 7. 项目结构

```
bousai/
├── backend/                      # Spring Boot（仅 AI 聊天）
│   ├── src/main/java/jp/kcgi/bousai/
│   │   ├── controller/           # ChatController, GlobalExceptionHandler
│   │   ├── service/              # ChatService
│   │   ├── dto/                  # ChatRequest / ChatResponse
│   │   ├── ai/                   # ChatAssistant + SpringAiChatAssistant + MockChatAssistant
│   │   ├── config/               # CORS、AiAssistantConfig（Claude/Mock 切替・RAG 配線）
│   │   └── BousaiApplication.java
│   ├── src/main/resources/application.properties
│   └── pom.xml
├── frontend/                     # React + Vite（GitHub Pages）
│   ├── src/{components,pages,api,i18n,locales,data,lib}/
│   ├── .github/workflows/deploy.yml
│   └── package.json
└── CLAUDE.md
```

---

## 8. 开发规范

**Java / Spring**
- 包名 `jp.kcgi.bousai.*`；类名 PascalCase，方法/变量 camelCase，常量 UPPER_SNAKE。
- Controller 只做参数校验与编排；业务进 Service。
- 对外一律返回 DTO。统一异常处理用 `@RestControllerAdvice`，REST 用规范 HTTP 状态码。
- 构造器注入（配 `final` 字段），**不用**字段注入 `@Autowired`。
- 注释 / Javadoc 用**日语**。

**API**
- 路径 `/api/v1/...`；请求/响应 JSON 字段 camelCase。后端当前对外端点：`/api/v1/chat`（非流式）、`/api/v1/chat/stream`（SSE 流式）、`/actuator/health`。

**前端**
- TypeScript 严格模式；组件函数式 + Hooks。
- 所有用户可见文案走 i18n，**禁止**硬编码语言文本。静态数据放 `src/data/`。
- 调后端：dev 用 Vite proxy（`/api`）；prod 用环境变量 `VITE_API_BASE_URL` 指向后端公网地址（见 §9）。

**安全 / 机密 / CORS**
- 机密（`ANTHROPIC_API_KEY` 等）只用**环境变量**，本地放 `.env`（已 gitignore）。
- **禁止**把任何密钥、`.env`、`node_modules/`、`target/` 提交到 git。
- 后端须配置 **CORS** 放行前端来源（GitHub Pages 域名 + 本地 dev `http://localhost:5173`）。

**Git**：分支 `main`（稳定）/ `feature/xxx`；提交信息用祈使句。

---

## 9. 部署

**前端（GitHub Pages）**
- `vite.config.ts` 的 `base` 设为仓库子路径（项目页：`/<repo>/`）。
- 通过 GitHub Actions（`.github/workflows/deploy.yml`）构建并发布到 Pages。
- 调后端地址用构建期环境变量 `VITE_API_BASE_URL`（在 Actions secrets/variables 配置）。

**后端**
- `mvn clean package` 打 jar → `java -jar` 运行，或打 Docker 镜像；部署到小型云主机 / 容器平台。
- 环境变量注入 LLM API key；配置 CORS 允许 GitHub Pages 域名。
- **无数据库依赖**，部署最简。

---

## 10. 常用命令

```bash
# 后端（在 backend/）
mvn spring-boot:run        # 本地运行（无需数据库）
mvn test                   # 测试
mvn clean package          # 打 jar

# 前端（在 frontend/）
npm install
npm run dev                # 开发服务器（Vite proxy 到本地后端）
npm run build              # 生产构建（用 VITE_API_BASE_URL）
npm run lint
```

---

## 11. 红线（Don'ts）

- ❌ 不在 AI 回答中编造避难所位置/电话/灾害指引（安全关键）。
- ❌ 避难所等坐标数据用**真实来源**（国土地理院），不得编造。
- ❌ 不提交任何密钥、`.env` 到 git。
- ❌ Controller 不写业务逻辑；对外不暴露内部结构，统一用 DTO。
- ❌ 前端不硬编码可见文案（必须 i18n）。
- ❌ 不擅自重新引入数据库 / JPA / 鉴权（已精简移除）；如需要先与团队确认。
