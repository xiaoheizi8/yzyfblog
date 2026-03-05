# 博客系统

🛠️ 前后端分离的现代化博客系统：JDK17 + Spring Boot 3 + MySQL + Redis，门户 UniApp，管理端 Vue3 + Ant Design + ECharts。

## 技术栈

| 模块       | 技术 |
|------------|------|
| 后端       | Spring Boot 3、MyBatis-Plus、MySQL 8、Redis、Elasticsearch（可选）、Sa-Token、Swagger、WebSocket、Docker、Nginx |
| 管理端前端 | Vue3、Pinia、Vue Router、Axios、Ant Design Vue、ECharts |
| 门户前端   | UniApp（H5 + 小程序） |

## 功能概览

- **文章**：Markdown 编辑、代码高亮与复制、图片预览、文章目录、推荐文章
- **评论**：支持表情、回复
- **留言**：弹幕墙展示
- **群聊**：WebSocket 群聊，支持表情、动图、文件、语音、图片
- **搜索**：可配置 MySQL 或 Elasticsearch，高亮分词（ES 需 IK 分词器）
- **上传**：可配置本地上传或七牛云 OSS
- **权限**：RBAC、动态菜单与权限、Sa-Token 认证
- **第三方登录**：Gitee/GitHub 等，降低注册成本
- **运维**：AOP 操作日志、博客配置与背景图、相册上传

## 项目结构

```
blog/
├── blog-backend/     # Spring Boot 3 后端
├── blog-admin/       # 管理端 Vue3
├── blog-portal/      # 门户 UniApp
└── README.md
```

## 快速开始

### 1. 数据库与中间件

- MySQL 8：执行 `blog-backend/src/main/resources/db/schema.sql` 建库建表
- Redis：默认 `localhost:6379`
- Elasticsearch（可选）：搜索模式为 `elasticsearch` 时使用，建议安装 IK 分词器

### 2. 后端

```bash
cd blog-backend
# 修改 application-dev.yml 中的数据库、Redis、ES 等配置
mvn spring-boot:run
```

- 接口文档：http://localhost:8080/api/swagger-ui.html
- 默认管理员：账号 `admin`，密码为 schema 中 BCrypt 密文对应的明文（常见示例为 `123456`），生产环境请务必修改

### 3. 管理端

```bash
cd blog-admin
npm install
npm run dev
```

- 访问：http://localhost:5174，代理已指向 `/api` -> `http://localhost:8080`

### 4. 门户（UniApp）

```bash
cd blog-portal
npm install
npm run dev:h5
```

- H5：http://localhost:5175（具体以 manifest 与 CLI 为准）

### 5. 配置说明

- **搜索模式**：`application.yml` 中 `blog.search-mode` 为 `mysql` 或 `elasticsearch`
- **上传模式**：`blog.upload-mode` 为 `local` 或 `minio`，MinIO 需配置 `blog.minio.*`
- **第三方登录**：配置 `blog.oauth2.gitee.*` 等，回调地址需与各平台一致

## 开发规范

- 后端遵循阿里巴巴 Java 开发规范
- 接口统一使用 `Result<T>` / `PageResult<T>` 包装
- 管理端接口以 `/admin/*` 需登录；门户以 `/portal/*` 为主，部分可匿名

## 部署建议

- 后端：打 jar 后使用 Docker 运行，Nginx 反向代理并配置 WebSocket
- 管理端 / 门户 H5：`npm run build` 后由 Nginx 托管静态资源
- 生产环境请修改默认密码、关闭 Swagger、配置 HTTPS 与 CORS
