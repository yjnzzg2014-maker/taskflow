# TaskFlow - Schedule & TodoList Management System

## 项目介绍

TaskFlow 是一款功能完善的桌面端日程管理与 ToDoList 系统，采用前后端分离架构开发。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- Spring Security 6
- JWT Authentication
- MySQL 8.0
- JPA/Hibernate

### 前端
- Vue 3.4.x
- TypeScript 5.x
- Vite 5.x
- Pinia (状态管理)
- Element Plus (UI 组件库)
- ECharts 5.x (数据可视化)

## 项目结构

```
taskflow/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/taskflow/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # REST 控制器
│   │   ├── service/           # 业务逻辑层
│   │   ├── repository/        # 数据访问层
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   ├── security/          # 安全认证
│   │   ├── exception/        # 异常处理
│   │   └── util/              # 工具类
│   ├── src/main/resources/
│   │   └── application.yml    # 配置文件
│   └── pom.xml                # Maven 依赖
│
├── frontend/                   # 前端 Vue 项目
│   ├── src/
│   │   ├── api/               # API 接口封装
│   │   ├── components/        # 公共组件
│   │   ├── layouts/           # 布局组件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia 状态管理
│   │   ├── styles/            # 全局样式
│   │   ├── types/             # TypeScript 类型
│   │   └── views/             # 页面组件
│   ├── package.json
│   └── vite.config.ts
│
└── README.md                   # 本文件
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 2. 后端配置

1. 创建 MySQL 数据库:
```sql
CREATE DATABASE taskflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/taskflow
    username: your_username
    password: your_password
```

3. 运行后端:
```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 3. 前端配置

1. 安装依赖:
```bash
cd frontend
npm install
```

2. 启动开发服务器:
```bash
npm run dev
```

前端将在 http://localhost:5173 启动

### 4. Docker 部署 (可选)

```bash
# 启动所有服务
docker-compose up -d
```

## 功能模块

### 1. 用户系统
- 用户注册/登录
- JWT 鉴权
- 角色权限 (管理员/普通用户)
- 用户信息管理

### 2. 日程管理
- 日历视图 (月/周/日)
- 事件 CRUD
- 重复任务 (每日/每周/每月/自定义)
- 提醒功能

### 3. ToDoList
- 任务 CRUD
- 优先级设置 (低/中/高/紧急)
- 状态管理 (未开始/进行中/已完成)
- 截止日期
- 标签系统
- 子任务支持

### 4. 仪表盘
- 今日任务统计
- 完成率图表
- 任务趋势图
- 数据可视化 (ECharts)

### 5. 高级功能
- 搜索功能
- 任务筛选
- 暗黑模式

## API 接口

### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/refresh | 刷新 Token |
| GET | /api/auth/me | 获取当前用户 |

### 日程接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/events | 获取日程列表 |
| POST | /api/events | 创建日程 |
| PUT | /api/events/{id} | 更新日程 |
| DELETE | /api/events/{id} | 删除日程 |
| GET | /api/events/calendar | 日历视图数据 |

### 任务接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tasks | 获取任务列表 |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务 |
| DELETE | /api/tasks/{id} | 删除任务 |
| GET | /api/tasks/search | 搜索任务 |

### 统计接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/stats/dashboard | 获取仪表盘数据 |

## 默认账户

注册新账户即可使用系统。

## 许可证

MIT License
