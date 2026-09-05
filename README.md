# 📔 九宫格记忆网（GridDiary）

一个以「九宫格图片日记」为核心的轻社交课程项目：用户可以撰写图文日记、多图上传并自动生成缩略图，以九宫格布局展示；支持日记增删改查、图片展开/旋转/查看原图、评论嵌套回复、点赞实时、分页浏览，以及搜索、热门排行榜、个人主页、站内私信等功能。

> 技术选型：前后端分离（Vue 3 SPA + RESTful API），Session 状态式登录。

---

## ✨ 功能特性

| 需求       | 功能                          | 说明                                                               |
| ---------- | ----------------------------- | ------------------------------------------------------------------ |
| ① 用户模块 | 注册 / 登录 / 退出 / 找回密码 | 用户名、邮箱唯一校验；邮箱格式校验；密码 **BCrypt 加盐存储**       |
| ② 首页设计 | 九宫格 + 分页                 | 展示所有用户公开日记：缩略图、标题、作者昵称、发布时间；响应式栅格 |
| ③ 写日记   | 标题/正文/多图                | 上传前**本地预览**，确认后保存并自动生成缩略图                     |
| ④ 日记管理 | 我的日记列表/查看/改/删       | 分页展示（含仅自己可见），仅作者可操作                             |
| ⑤ 日记查看 | 图片操作                      | 展开/收缩、查看原图、左转/右转（CSS rotate）；支持全部或个人视图   |
| ⑥ 社交     | 评论 + 点赞                   | 评论**两级嵌套回复**；点赞数**实时局部刷新**（Axios 无整页刷新）   |
| ⑦ 分页     | 首页 / 列表                   | 每页固定数量，可切换页码                                           |
| ⑧ 扩展     | 搜索 / 排行 / 主页 / 私信     | 关键词搜索、热门 Top N、个人主页、站内私信 + 顶栏未读角标          |
| 加分       | 个人设置                      | 修改昵称、上传头像、修改密码（校验原密码）                         |

---

## 🧰 技术栈

### 后端 `backend/`

- **Spring Boot 4.0.8**（Java 25）· Spring MVC（RESTful）
- **MyBatis + MySQL 8**（预编译 SQL，防注入）+ Mapper XML
- Session 登录（HttpSession）、**BCrypt**（spring-security-crypto）
- Bean Validation 参数校验、Lombok、Maven
- 统一响应 `Result{code,message,data}`、全局异常处理、`@PublicApi` 匿名放行
- 分层：Controller → Service(接口+`impl`) → Mapper

### 前端 `frontend/`

- **Vue 3**（Composition API）+ **Vite** + **Element Plus** + **Pinia** + **Vue Router** + **Axios**
- 图片上传本地预览（`URL.createObjectURL`）、灯箱旋转预览
- Axios 拦截器统一处理 `{code,message,data}` 与 401；`/api`、`/upload` 代理到后端（同源保 Session Cookie）

### 数据库

- 库名 `grid_diary`（utf8mb4），6 张表：`user`、`diary`、`diary_image`、`comment`、`diary_like`、`message`
- 外键级联删除、唯一索引防重复点赞、`like_count` 冗余计数
- 建表脚本见 `sql/schema.sql`

---

## 📁 目录结构

```
GridDiary/
├─ backend/                # Spring Boot 后端
│  ├─ src/main/java/com/flandreyu/
│  │  ├─ controller/       # 接口层
│  │  ├─ service/          # 业务接口
│  │  │  └─ impl/          # 业务实现
│  │  ├─ mapper/           # MyBatis 接口
│  │  ├─ entity/ dto/ vo/  # 实体 / 入参 / 出参
│  │  ├─ common/           # Result/异常/全局处理/注解
│  │  ├─ config/           # 拦截器/CORS/密码编码/静态映射
│  │  └─ util/             # 图片存储/会话工具
│  ├─ src/main/resources/mapper/*.xml
│  └─ src/main/resources/application.yaml
├─ frontend/               # Vue3 前端
│  ├─ src/
│  │  ├─ views/            # 页面（首页/写日记/详情/我的/排行/主页/私信/设置…）
│  │  ├─ components/       # DiaryCard 等
│  │  ├─ api/              # axios 封装与各模块接口
│  │  ├─ store/ router/ utils/
│  └─ vite.config.js       # /api、/upload 代理
├─ sql/schema.sql          # 建库建表脚本
├─ requirement.doc         # 原始需求（保留）
└─ 需求文档.md             # 优化版需求文档
```

---

## 🚀 快速开始

### 1. 准备数据库（MySQL 8）

```bash
mysql -u root -p < sql/schema.sql
```

- 会自动创建 `grid_diary` 库与 6 张表。

### 2. 配置后端连接

编辑 `backend/src/main/resources/application.yaml`，把账号密码改成你自己的：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/grid_diary?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的数据库密码
```

### 3. 启动后端（默认 8080）

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

> 图片上传默认存到运行目录 `./upload/`，可用 `--app.upload-dir=绝对路径` 覆盖。

### 4. 启动前端（默认 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 **http://localhost:5173**，先注册一个账号即可体验完整流程。

---

## 🔌 主要接口一览（前缀 `/api`）

| 模块     | 方法与路径                                                             | 说明                               |
| -------- | ---------------------------------------------------------------------- | ---------------------------------- |
| 用户     | `POST /user/register` `POST /user/login` `POST /user/logout`           | 注册 / 登录 / 退出                 |
| 用户     | `GET /user/me` `GET /user/{id}`                                        | 当前用户 / 公开资料                |
| 用户     | `PUT /user/profile` `PUT /user/password`                               | 改资料(昵称头像) / 改密码          |
| 找回密码 | `POST /user/forgot-password` `POST /user/reset-password`               | 验证码(演示返回) / 重置            |
| 日记     | `GET /diary` `GET /diary/{id}` `GET /diary/hot`                        | 公开列表(分页/搜索) / 详情 / 排行  |
| 日记     | `GET /diary/mine` `POST /diary` `PUT /diary/{id}` `DELETE /diary/{id}` | 我的 / 新建 / 改 / 删              |
| 上传     | `POST /upload/image`                                                   | 多图上传（自动生成缩略图）         |
| 评论     | `GET/POST /diary/{id}/comments`                                        | 评论树 / 发表（支持回复）          |
| 点赞     | `POST/DELETE/GET /diary/{id}/like`                                     | 点赞 / 取消 / 状态（返回实时计数） |
| 私信     | `GET /message/conversations` `GET /message/unread-count`               | 会话列表 / 未读数                  |
| 私信     | `GET /message/with/{peerId}` `POST /message`                           | 聊天记录(自动已读) / 发送          |

---

## 📌 说明与注意事项

- **登录态**：基于 Session（Cookie `JSESSIONID`），开发期前端经 Vite 代理保持同源；生产部署建议 Nginx 反代。
- **权限**：匿名可访问的接口用 `@PublicApi` 标注；日记修改/删除、评论删除等校验“本人或作者”；私密日记仅作者可见。
- **安全**：SQL 全部 MyBatis 预编译；密码 BCrypt；上传仅允许 jpg/png 且 ≤5MB。
- **找回密码为演示实现**：6 位验证码存内存并直接返回给前端（真实项目应改为邮件发送）。
- **演示提醒**：后端 Session 默认约 30 分钟失效，长时间未操作会提示重新登录（非 bug）。

---

## 🖼 演示数据建议

如需让首页/排行/私信角标看起来更丰满，可自行多注册几个账号并发布带图日记、互相点赞评论私信；或用前端页面逐个创建即可。

## 📄 文档

- `需求文档.md` —— 优化版（现代技术栈）需求与接口规划
- `requirement.doc` —— 原始课程需求（功能点 1~8）
