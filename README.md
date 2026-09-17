# 📔 九宫格记忆网（GridDiary）

[![CI](https://github.com/FLandreyu/GridDiary/actions/workflows/ci.yml/badge.svg)](https://github.com/FLandreyu/GridDiary/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.8-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5-42b883)
![Swagger](https://img.shields.io/badge/API%20Docs-Swagger%20UI-85ea2d)

一个以「九宫格图片日记」为核心的轻社交课程项目：用户可以撰写图文日记、多图上传并自动生成缩略图，以九宫格布局展示；支持日记增删改查、图片展开/旋转/查看原图、评论嵌套回复、点赞实时、分页浏览，以及搜索、热门排行榜、个人主页、站内私信等功能。

> 技术选型：前后端分离（Vue 3 SPA + RESTful API），Session 状态式登录。
> 接口文档：后端启动后访问 <http://localhost:8080/swagger-ui.html>

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

### ✨ 额外做的（超出原始需求）

| 功能           | 说明                                                                                           |
| -------------- | ---------------------------------------------------------------------------------------------- |
| 分类与标签     | 写日记时可填分类 + 最多 5 个标签；首页/列表展示标签，点击标签筛选（`?tag=xxx`），侧栏标签云 |
| 相册页         | `GET /api/diary/gallery` 汇总所有公开日记的图片，CSS 多列瀑布流 + 懒加载                       |
| 写作热力图     | GitHub 贡献图风格的年度写作日历，含总篇数/总字数/活跃天数/**连续写作天数**                     |
| 站点统计       | 公开日记数、用户数、总字数、获赞、评论、分类数、运行天数、最近更新                             |
| 个人主页统计   | 公开日记 / 获赞 / 收到评论 / 加入天数（`GET /api/user/{id}/stats`）                            |
| 每日打卡       | 一天一次打卡 + 今日运势（大吉/中吉/小吉/平），日历上同时标记「有日记 / 已打卡」               |
| 日夜主题       | `html.dark` + CSS 变量，全站一套变量切换浅色/暗色，跟随系统偏好                                |
| 响应式         | 两栏骨架 + 窄屏抽屉导航 + 网格自适应；骨架屏、路由过渡、回到顶部                               |
| 无障碍         | 语义化标签、`aria-*`、`prefers-reduced-motion` 降低动效                                        |
| 接口文档       | springdoc-openapi（Swagger UI），按模块分组（用户/日记/私信/统计）                            |
| 持续集成       | GitHub Actions：后端 `mvnw package` + 前端 `npm ci && npm run build`                          |

---

## 🧰 技术栈

### 后端 `backend/`

- **Spring Boot 4.0.8**（Java 25）· Spring MVC（RESTful）
- **MyBatis + MySQL 8**（预编译 SQL，防注入）+ Mapper XML
- Session 登录（HttpSession）、**BCrypt**（spring-security-crypto）
- Bean Validation 参数校验、Lombok、Maven
- 统一响应 `Result{code,message,data}`、全局异常处理、`@PublicApi` 匿名放行
- 分层：Controller → Service(接口+`impl`) → Mapper
- **springdoc-openapi 3.0.3**（Swagger UI，3.x 才兼容 Spring Boot 4）

### 前端 `frontend/`

- **Vue 3**（Composition API）+ **Vite** + **Element Plus** + **Pinia** + **Vue Router** + **Axios**
- 字体：标题用 **站酷快乐体**、正文用 **站酷小薇**（`@fontsource/*` 本地打包，开源可商用，中文按 `unicode-range` 分片按需加载）
- Element Plus **按需引入**（`unplugin-vue-components`）：主包 317 kB / CSS 81 kB（对比全量引入的 1120 kB / 392 kB）
- 图片上传本地预览（`URL.createObjectURL`）、灯箱旋转预览
- Axios 拦截器统一处理 `{code,message,data}` 与 401；`/api`、`/upload` 代理到后端（同源保 Session Cookie）

### 数据库

- 库名 `grid_diary`（utf8mb4），7 张表：`user`、`diary`、`diary_image`、`comment`、`diary_like`、`message`、`checkin`
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
├─ package.ps1             # 一键打包（前端→jar→exe）
├─ tools/                  # 打包工具：图标生成、幂等 SQL 生成、调试启动器配置
├─ packaging/              # 打包时复制进产物的文件（外部配置模板、使用说明）
├─ dist/GridDiary/         # 打包产物（免安装绿色版，双击 GridDiary.exe）
├─ requirement.doc         # 原始需求（保留）
└─ 需求文档.md             # 优化版需求文档
```

---

## 🚀 快速开始

### 1. 准备数据库（MySQL 8）

```bash
mysql -u root -p < sql/schema.sql
```

- 会自动创建 `grid_diary` 库与 7 张表。
- 用打包好的 exe 运行时不用手工执行这一步（程序会自动建库建表）。

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

## � 打包成「双击即用」的 exe

把前端构建产物塞进后端 JAR，再用 JDK 自带的 `jpackage` 生成**内置 JRE 的应用镜像** —— 目标电脑无需安装 Java。

```powershell
# 项目根目录执行（需要 JDK 17+ 与 Node.js）
powershell -ExecutionPolicy Bypass -File package.ps1
```

产物在 `dist/GridDiary/`，整个目录可拷贝到任意 Windows 电脑（目标机只要装了 MySQL 8）：

| 文件 / 目录               | 说明                                                      |
| ------------------------- | --------------------------------------------------------- |
| `GridDiary.exe`           | **双击运行**，启动后自动打开浏览器 http://localhost:8080  |
| `GridDiaryDebug.exe`      | 调试版（带控制台窗口，能看到启动日志），排错用            |
| `config/application.yaml` | 运行配置：数据库账号密码、端口，优先级最高                |
| `upload/`                 | 日记图片（原图 + 缩略图）                                 |
| `logs/griddiary.log`      | 运行日志；启动失败时会额外生成 `logs/启动失败.txt`        |
| `app/`、`runtime/`        | 程序本体（jar）与内置 Java 运行环境                       |
| `README.txt`              | 给使用者看的说明（运行步骤 + 常见问题）                   |

打包脚本 `package.ps1` 做的事：

1. `npm run build` → 把 `frontend/dist` 复制到 `backend/src/main/resources/static`
2. 由 `sql/schema.sql` 生成**幂等**建表脚本 `db/init.sql`（去掉 `DROP TABLE`、改 `IF NOT EXISTS`）
3. `mvnw -DskipTests package` 打出可执行 jar（并校验前端页面确实进了 jar）
4. 生成 `.ico` 应用图标（`tools/MakeIcon.java`，图形与 `frontend/public/images/logo.svg` 一致）
5. `jpackage --type app-image` 生成内置 JRE 的 exe（主程序 + 调试版）
6. 补齐 `config/`、`upload/`（带上现有图片）、`README.txt`

常用参数：`-SkipFrontend`（前端没改动，跳过构建）/ `-Zip`（额外压缩成 zip）/ `-AppVersion 1.1.0`。

> **首次运行自动建库建表**：JDBC URL 带 `createDatabaseIfNotExist=true`，启动时执行 `classpath:db/init.sql`，
> 所以只要 MySQL 在运行、账号密码正确就能直接用（脚本全是 `IF NOT EXISTS`，不会清空已有数据）。
>
> 需要带开始菜单/卸载的 `.exe` 安装包时，安装 [WiX Toolset](https://wixtoolset.org/) 后把
> `--type app-image` 改成 `--type exe` 即可（本机未装 WiX，故默认产出免安装绿色版）。

---

## �🔌 主要接口一览（前缀 `/api`）

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
| 统计     | `GET /stats` `GET /stats/tags`                                         | 站点统计 / 标签云（匿名可见）      |
| 统计     | `GET /stats/calendar` `GET /stats/heatmap`                             | 当月日历打点 / 写作热力图          |
| 相册     | `GET /diary/gallery`                                                   | 公开日记图片（分页，瀑布流用）     |
| 打卡     | `GET /checkin/today` `POST /checkin`                                   | 今日打卡状态 / 打卡（含今日运势）  |

> 接口文档（Swagger UI）：后端启动后访问 <http://localhost:8080/swagger-ui.html>，
> 可按「用户 / 日记 / 私信 / 统计与打卡」分组查看，并直接在线调试（需先登录拿到 Session）。

---

## ✅ 持续集成

`.github/workflows/ci.yml`：推送 `main` 或提 PR 时自动运行

| Job      | 内容                                                                 |
| -------- | -------------------------------------------------------------------- |
| backend  | Temurin JDK 25 + Maven 缓存 → `./mvnw -B -DskipTests package`        |
| frontend | Node 22 + npm 缓存 → `npm ci` → `npm run build`（产物作为 artifact） |

> 单元测试需要本地 MySQL，CI 中先跳过；本地可用 `./mvnw test` 自行运行。

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

- `需求文档.md` —— 需求与接口规划
- Swagger UI —— 运行时 <http://localhost:8080/swagger-ui.html>；JSON 规范见 `/v3/api-docs`
