# 快练习题系统 📚

> 题目集管理、题库导入（含 AI 解析）、在线练习、错题与练习记录的 Web 应用

![Java](https://img.shields.io/badge/Java-51.1%25-ED8936?style=flat-square)
![Vue](https://img.shields.io/badge/Vue-44.5%25-4FC08D?style=flat-square)
![JavaScript](https://img.shields.io/badge/JavaScript-4.3%25-F7DF1E?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

## 📖 项目简介

本项目是一个本科毕业设计项目，基于 SpringBoot 和 Vue3 开发。系统支持用户自定义大模型 API，能够快速识别和导入图片、文本、文件等各种类型的习题试卷，并提供丰富的练习、组卷、错题收集等功能。

### ✨ 核心特性

- 🤖 **AI 题目识别**：支持自定义大模型 API，快速识别和导入各种格式习题
- 📝 **题库管理**：智能题库导入、分类管理、版本控制
- ✅ **在线练习**：互动式练习体验，实时反馈
- 🎯 **自主组卷**：灵活组合试卷，支持多种出题方式
- ❌ **错题收集**：自动收集错题，便于查看和复习
- 📊 **练习统计**：详细的练习记录和数据分析

---

## 🛠️ 技术栈

| 部分 | 技术栈 |
|------|--------|
| **前端** | Vue 3、Vite、Naive UI、Pinia、Vue Router |
| **后端** | Spring Boot 3、Spring Security、JPA、JWT |
| **数据库** | MySQL 8.x |
| **工具** | Maven、npm |

---

## 📋 系统要求

- **JDK** 17+ 
- **Node.js** 18+（推荐）
- **MySQL** 8.x
- **Maven** 3.6+

---

## 🚀 快速开始

### 1️⃣ 数据库配置

在 MySQL 中创建数据库：

```sql
CREATE DATABASE end_of_term_revision CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入初始化脚本（在 `database/` 目录下）：

```bash
# 选择其中一个脚本导入
mysql -u root -p end_of_term_revision < database/init_database.sql
# 或
mysql -u root -p end_of_term_revision < database/end_of_term_revision.sql
```

> **注意**：`application.yml` 中 `ddl-auto: none` 表示表结构以脚本为准，不会自动生成。

### 2️⃣ 后端启动

编辑配置文件 `java-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/end_of_term_revision
    username: root          # 修改为你的数据库用户名
    password: your_password # 修改为你的数据库密码
    
jwt:
  secret: your-secret-key   # 修改为强密钥，生产环境勿提交真实密码
```

启动后端服务：

```bash
cd java-backend
mvn spring-boot:run
```

✅ 默认端口：**8001**

或使用 IDE 直接运行 `main` 方法。

### 3️⃣ 前端启动

```bash
cd fronted

# 安装依赖
npm install

# 开发模式
npm run dev
```

✅ 默认地址：**http://localhost:3000**

通过 Vite 代理自动将 `/api` 请求转发到 `http://localhost:8001`。

### 🔧 前端环境变量（可选）

在 `fronted/` 目录创建 `.env` 文件：

```env
VITE_API_TARGET=http://localhost:8001  # 后端地址
VITE_PORT=3000                         # 前端端口
```

### 4️⃣ 生产构建

```bash
cd fronted
npm run build
```

输出文件在 `dist/` 目录中，可部署到 web 服务器。

---

## 📁 项目结构

```
ExercisePracticePlatform/
├── fronted/                 # Vue 3 前端应用
│   ├── src/
│   ├── public/
│   ├── vite.config.ts
│   ├── package.json
│   └── ...
├── java-backend/            # Spring Boot 后端应用
│   ├── src/
│   ├── pom.xml
│   └── ...
├── database/                # MySQL 初始化脚本
│   ├── init_database.sql
│   └── end_of_term_revision.sql
├── README.md
└── .gitignore
```

---

## ⚙️ 配置说明

### 文件上传

文件上传的存储路径由 `application.yml` 中的 `app.upload-dir` 配置决定（默认为 `uploads`）。

### 大模型 API 配置

使用图片/文件 AI 识别功能前，需在系统内配置大模型 API：

1. 登录系统管理后台
2. 进入**系统设置** → **API 配置**
3. 填写大模型的基础 URL、API Key 等信息
4. 保存并测试连接

支持主流大模型服务（如 OpenAI、阿里云、腾讯云等）。

---

## 📚 主要功能说明

### 题库管理
- 导入题目（文本/图片/文件）
- AI 自动识别和解析题目
- 题目分类和标签管理
- 题目编辑和批量操作

### 练习功能
- 在线做题体验
- 实时答案反馈
- 练习进度跟踪
- 详细的统计分析

### 自主组卷
- 灵活的出题规则配置
- 支持按难度、类型、知识点等条件组卷
- 随机和手动两种组卷方式
- 试卷预览和下载

### 错题管理
- 自动收集错题
- 错题分析和归类
- 针对性复习建议
- 错题统计

---

## 🐛 常见问题

**Q: 如何修改默认端口？**

A: 
- 后端：修改 `application.yml` 中的 `server.port`
- 前端：修改 `.env` 中的 `VITE_PORT` 或在启动时使用 `npm run dev -- --port 3001`

**Q: 连接数据库失败？**

A: 检查以下几点：
- MySQL 服务是否启动
- 数据库用户名和密码是否正确
- 数据库是否已创建并导入脚本

**Q: AI 识别功能不可用？**

A: 确保已在系统设置中配置了大模型 API 的基础 URL 和 Key。




