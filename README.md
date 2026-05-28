# 快练习题系统

题目集管理、题库导入（含 AI 解析）、在线练习、错题与练习记录的 Web 应用。毕业设计项目。

## 技术栈

| 部分 | 技术 |
|------|------|
| 前端 | Vue 3、Vite、Naive UI、Pinia、Vue Router |
| 后端 | Spring Boot 3、Spring Security、JPA、JWT |
| 数据库 | MySQL |

## 环境要求

- **JDK** 17+
- **Node.js** 18+（建议）
- **MySQL** 8.x，并创建数据库（默认库名见下方配置）

## 快速开始

### 1. 数据库

在 MySQL 中创建数据库，例如：

```sql
CREATE DATABASE end_of_term_revision CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入 `database/` 目录下的 SQL（如 `init_database.sql` 或 `end_of_term_revision.sql`）完成建表与初始化。`application.yml` 中 `ddl-auto: none` 表示表结构以脚本为准。

### 2. 后端

编辑 `java-backend/src/main/resources/application.yml`：

- 修改 `spring.datasource` 中的用户名、密码、库名
- 按需修改 `jwt.secret`
- 生产环境勿提交真实密码

启动：

```bash
cd java-backend
mvn spring-boot:run
```

默认端口：**8001**。

### 3. 前端

```bash
cd fronted
npm install
npm run dev
```

默认开发地址：**http://localhost:3000**，通过 Vite 代理将 `/api` 转发到 `http://localhost:8001`。

可选环境变量（`.env`）：

- `VITE_API_TARGET`：后端地址，默认 `http://localhost:8001`
- `VITE_PORT`：前端端口，默认 `3000`

### 4. 生产构建

```bash
cd fronted
npm run build
```

## 目录说明

```
快练练习系统/
├── fronted/          # Vue 前端
├── java-backend/     # Spring Boot 后端
├── database/         # MySQL 初始化脚本
└── README.md
```

## 说明

- 上传文件目录由 `app.upload-dir` 配置（默认 `uploads`）。
- 使用文档/图片 AI 导入前，需在系统内配置大模型 API（基础 URL、Key 等）。
