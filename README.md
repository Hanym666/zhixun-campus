# 智寻校园（Zhixun Campus）

面向校园失物招领场景的微服务项目。后端基于 Java 21、Spring Boot、Spring Security、Nacos、MyBatis、Redis、Elasticsearch、Spring AI 与 Redis Stack；前端建议使用 Vue 3 + TypeScript + Vite，并放在根目录的 `zhixun-web` 中。

## 项目结构

| 目录 | 说明 | 默认端口 |
| --- | --- | --- |
| `zhixun-gateway` | 前端统一 API 入口 | 8080 |
| `zhixun-user-service` | 注册、登录、令牌与用户信息 | 8081 |
| `zhixun-item-service` | 物品、图片、智能匹配、认领、会话与通知 | 8082 |
| `zhixun-ai-service` | 特征提取和向量检索 | 8083 |
| `zhixun-common` | 公共响应、异常、分页和枚举 | - |
| `sql` | 数据库初始化脚本 | - |
| `zhixun-web` | Vue 前端（由前端成员创建） | 5173 |

## 开发环境

- JDK 21
- Maven 3.9+
- IntelliJ IDEA 2024.3.5 或更新版本
- Docker Desktop（推荐，用于启动基础设施）
- Ollama（启用 AI 功能时需要）
- Node.js 20+（仅前端成员需要）

## 首次启动

### 1. 获取代码和配置

```powershell
git clone https://github.com/Loser-Lii/zhixun-campus.git
cd zhixun-campus
Copy-Item .env.example .env
```

打开 `.env`，至少修改 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD` 和 `JWT_SECRET`。`.env` 已被 Git 忽略，不要把真实密码提交到仓库。

### 2. 启动基础设施

```powershell
docker compose up -d
docker compose ps
```

Compose 会启动 MySQL、Redis、Redis Stack、Nacos 和 Elasticsearch。MySQL 数据目录第一次创建时会自动执行 `sql/01_init_schema.sql`。

如果本机已经安装并启动了同类服务，可以不使用 Compose，但端口和数据库名称必须与 `.env` 一致。

### 3. 准备 AI 模型

安装并启动 Ollama 后执行：

```powershell
ollama pull qwen2.5:1.5b
ollama pull nomic-embed-text
```

前端页面开发暂时不需要 AI 时，可以先不启动 `zhixun-ai-service`；智能特征与向量匹配功能将不可用。

### 4. 验证后端构建

```powershell
$env:JAVA_HOME = "你的JDK21目录"
mvn clean test
```

### 5. 启动 Java 服务

在每一个新的 PowerShell 终端中先加载 `.env`：

```powershell
. .\scripts\load-env.ps1
```

然后分别启动四个服务，建议顺序如下：

```powershell
mvn -pl zhixun-ai-service spring-boot:run
mvn -pl zhixun-user-service spring-boot:run
mvn -pl zhixun-item-service spring-boot:run
mvn -pl zhixun-gateway spring-boot:run
```

在 IDEA 中也可以直接运行各模块的 `*Application` 类，但需要在运行配置的“环境变量”中填写 `.env` 里的变量。四个后端启动类分别位于：

- `com.zhixun.ai.ZhixunAiApplication`
- `com.zhixun.user.ZhixunUserApplication`
- `com.zhixun.item.ZhixunItemApplication`
- `com.zhixun.gateway.ZhixunGatewayApplication`

### 6. 检查服务

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/api/categories
```

所有前端请求统一访问网关 `http://localhost:8080`，不要直接依赖 8081、8082、8083。

## 前端初始化

前端成员从 `develop` 创建功能分支后执行：

```powershell
npm create vite@latest zhixun-web -- --template vue-ts
cd zhixun-web
npm install
npm install axios pinia vue-router
```

开发环境把 `/api` 和 `/uploads` 代理到 `http://localhost:8080`。接口清单与鉴权规则见 [`docs/API.md`](docs/API.md)。

## 日常协作

- `main`：稳定版本，只接受 Pull Request。
- `develop`：日常集成分支。
- `feature/*`：功能开发，例如 `feature/frontend-auth`。
- `fix/*`：缺陷修复。

详细提交与合并规范见 [`CONTRIBUTING.md`](CONTRIBUTING.md)。

## 常见问题

### 端口被占用

修改 `.env` 中对应端口；如果修改网关或业务服务端口，同时检查 Nacos 注册信息与前端代理。

### 数据表没有创建

初始化脚本只在新的 MySQL 数据卷上自动执行。已有数据库请在 MySQL 客户端中手动执行 `sql/01_init_schema.sql`。

### 健康检查显示 Elasticsearch、Redis 或 AI 不可用

先执行 `docker compose ps`，再检查 Ollama 是否运行以及两个模型是否已经下载。基础业务可以独立开发，但智能匹配需要 Elasticsearch、AI 服务和 Redis Stack 全部可用。
