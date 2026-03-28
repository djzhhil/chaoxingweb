# Chaoxing Web

超星学习通自动化工具 - Java 版本

## 项目结构

```
chaoxingweb/
├── auth/              # 认证模块
│   ├── entity/       # 实体类
│   ├── dto/          # 数据传输对象
│   ├── vo/           # 视图对象
│   ├── repository/   # 数据访问层
│   ├── service/      # 服务层
│   ├── controller/   # 控制器层
│   ├── config/       # 配置类
│   ├── util/         # 工具类
│   └── exception/    # 异常类
└── chaoxing/         # 超星模块（TODO）
```

## 模块说明

### Auth 模块

认证模块，负责用户注册、登录、权限管理。

**功能：**
- 用户注册
- 用户登录
- 用户信息管理
- 权限管理
- JWT Token 生成和校验

**API：**
- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/me` - 获取当前用户信息
- `PUT /api/users/me` - 更新用户信息
- `POST /api/users/change-password` - 修改密码

### Chaoxing 模块

超星模块，负责超星学习通自动化功能。

**功能：**
- 超星账号绑定
- 超星账号解绑
- 查询绑定的超星账号
- 使用绑定的超星账号登录
- 验证码识别
- 密码加密
- Cookie 管理
- 获取课程列表
- 获取任务点列表
- 任务执行（视频、文档、直播）
- 题库答题
- 通知服务

**状态：** TODO

## 技术栈

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Security 6.x**
- **Spring Data JPA**
- **MySQL 8.0**
- **JWT**
- **Maven**

## 快速开始

### 前置条件

- JDK 21+
- Maven 3.6+
- MySQL 8.0+

### 数据库初始化

```sql
CREATE DATABASE chaoxing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 运行项目

```bash
# 编译项目
mvn clean install

# 运行 auth 模块
cd auth
mvn spring-boot:run

# 运行 chaoxing 模块
cd chaoxing
mvn spring-boot:run
```

## 配置说明

### Auth 模块配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chaoxing
    username: root
    password: root

jwt:
  secret: chaoxingweb-secret-key-2024
  expiration: 86400000 # 24小时
```

## 开发计划

### Auth 模块

- [x] 用户注册
- [x] 用户登录
- [x] 用户信息管理
- [x] 权限管理
- [x] JWT Token 生成和校验
- [ ] Spring Security 集成
- [ ] 单元测试

### Chaoxing 模块

- [ ] 超星账号绑定
- [ ] 超星账号解绑
- [ ] 查询绑定的超星账号
- [ ] 使用绑定的超星账号登录
- [ ] 验证码识别
- [ ] 密码加密
- [ ] Cookie 管理
- [ ] 获取课程列表
- [ ] 获取任务点列表
- [ ] 任务执行（视频、文档、直播）
- [ ] 题库答题
- [ ] 通知服务

## 许可证

GPL-3.0

## 作者

耀（djzhhil）
