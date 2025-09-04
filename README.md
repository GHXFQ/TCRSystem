# 教学课程资源管理系统 (TCR System)

## 📋 项目简介

教学课程资源管理系统（Teaching Course Resources Management System）是一个基于Spring Boot + Vue 3的全栈Web应用，旨在为教育机构提供完整的课程资源管理解决方案。系统支持学生、教师、管理员三种角色，提供课程管理、资料上传下载、作业管理、反馈系统等功能。

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot 2.7.3
- **数据库**: MySQL 5.7+
- **ORM**: MyBatis Plus 3.5.2
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **文档**: Knife4j (Swagger)
- **文件存储**: MinIO 8.4.3
- **构建工具**: Maven
- **Java版本**: JDK 1.8

### 前端技术栈
- **框架**: Vue 3.2.13
- **UI组件**: Element Plus 2.2.5
- **路由**: Vue Router 4.0.16
- **状态管理**: Vuex 4.0.2
- **HTTP客户端**: Axios 1.8.3
- **图表**: ECharts 5.6.0
- **构建工具**: Vue CLI 5.0

## 🚀 功能特性

### 👨‍🎓 学生端功能
- 课程选择与管理
- 课程资料下载
- 作业提交与查看
- 课程反馈提交
- 个人信息管理

### 👨‍🏫 教师端功能
- 课程资料上传与管理
- 作业发布与批改
- 学生作业查看
- 课程反馈查看
- 资源协同共享
- 材料审核

### 👨‍💼 管理员功能
- 用户管理（学生、教师）
- 课程管理
- 材料审核与统计
- 教学材料任务管理
- 系统数据统计
- 教务管理

## 📁 项目结构

```
TCRSystem/
├── backend/                    # 后端Spring Boot项目
│   ├── src/main/java/com/tcr/system/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── entity/           # 实体类
│   │   ├── mapper/           # MyBatis映射器
│   │   ├── service/          # 业务逻辑层
│   │   ├── dto/              # 数据传输对象
│   │   ├── vo/               # 视图对象
│   │   └── utils/            # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/           # MyBatis XML映射文件
│   │   └── application.yml   # 应用配置
│   └── pom.xml              # Maven依赖配置
├── frontend/                  # 前端Vue项目
│   ├── src/
│   │   ├── api/              # API接口
│   │   ├── components/       # 公共组件
│   │   ├── layouts/          # 布局组件
│   │   ├── router/           # 路由配置
│   │   ├── store/            # Vuex状态管理
│   │   ├── utils/            # 工具函数
│   │   └── views/            # 页面组件
│   │       ├── admin/        # 管理员页面
│   │       ├── teacher/      # 教师页面
│   │       └── student/      # 学生页面
│   ├── public/               # 静态资源
│   ├── package.json          # npm依赖配置
│   └── vue.config.js         # Vue CLI配置
├── avatars/                   # 用户头像存储
├── tcr_system.sql            # 数据库初始化脚本
├── start.bat                 # 一键启动脚本
└── README.md                 # 项目说明文档
```

## 🛠️ 环境要求

- **Java**: JDK 1.8+
- **Node.js**: 14.0+
- **MySQL**: 5.7+
- **Redis**: 6.0+ (可选)
- **Maven**: 3.6+
- **npm**: 6.0+

## 📦 安装部署

### 1. 克隆项目
```bash
git clone https://github.com/GHXFQ/TCRSystem.git
cd TCRSystem
```

### 2. 数据库配置
1. 创建MySQL数据库：
```sql
CREATE DATABASE tcr_system CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 导入数据库脚本：
```bash
mysql -u root -p tcr_system < tcr_system.sql
```

3. 修改数据库配置（如果密码不是默认值）：
编辑 `backend/src/main/resources/application.yml`
```yaml
spring:
  datasource:
    username: root
    password: your_mysql_password  # 修改为你的MySQL密码
```

### 3. 后端启动
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```
后端服务将在 `http://localhost:8080` 启动

### 4. 前端启动
```bash
cd frontend
npm install
npm run serve
```
前端服务将在 `http://localhost:3030` 启动

### 5. 一键启动（Windows）
双击运行 `start.bat` 文件，将自动启动前后端服务。

## 🔧 配置说明

### 后端配置
主要配置文件：`backend/src/main/resources/application.yml`

- **数据库配置**: MySQL连接信息
- **Redis配置**: 缓存服务配置
- **JWT配置**: 认证令牌配置
- **文件上传**: 文件存储路径配置
- **跨域配置**: CORS设置

### 前端配置
主要配置文件：`frontend/vue.config.js`

- **开发服务器**: 端口3030
- **代理配置**: API请求代理到后端8080端口

## 🌐 系统访问

- **前端地址**: http://localhost:3030
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html (Knife4j)

### 默认账户
系统提供以下测试账户：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理员 |
| 教师 | teacher | teacher123 | 教师用户 |
| 学生 | student | student123 | 学生用户 |

## 📊 数据库设计

系统包含以下主要数据表：

- **用户表** (user): 存储用户基本信息
- **课程表** (course): 课程信息管理
- **材料表** (material): 教学材料信息
- **作业表** (assignment): 作业任务管理
- **提交表** (assignment_submission): 学生作业提交
- **反馈表** (feedback): 课程反馈信息
- **审核表** (material_review): 材料审核记录

## 🔐 安全特性

- **JWT认证**: 基于Token的用户认证
- **角色权限**: 三级权限控制（学生/教师/管理员）
- **路由守卫**: 前端路由权限验证
- **CORS配置**: 跨域请求安全控制
- **SQL注入防护**: MyBatis Plus参数化查询

## 🚧 开发指南

### 后端开发
1. 实体类使用Lombok注解简化代码
2. 使用MyBatis Plus进行数据库操作
3. 统一返回结果格式（Result类）
4. 全局异常处理机制

### 前端开发
1. 组件化开发，复用性强
2. 统一的API请求封装
3. 响应式布局设计
4. Element Plus组件库

## 📈 性能优化

- **数据库索引**: 关键字段建立索引
- **Redis缓存**: 热点数据缓存
- **分页查询**: 大数据量分页处理
- **文件上传**: 支持大文件分片上传
- **前端优化**: 路由懒加载、组件按需引用

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢所有为本项目做出贡献的开发者和使用者！

---

⭐ 如果这个项目对你有帮助，请给它一个星标！