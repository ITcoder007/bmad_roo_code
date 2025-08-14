# 证书生命周期管理系统 - 后端

基于 Spring Boot 2.7 + MyBatis Plus 的企业级后端服务。

## 技术栈

- Java 8
- Spring Boot 2.7.x
- Spring Security
- MyBatis Plus 3.5.x
- MySQL 8.0
- JWT 认证
- Swagger/OpenAPI

## 开发

```bash
# 安装依赖
mvn dependency:resolve

# 启动开发服务器
mvn spring-boot:run

# 运行测试
mvn test

# 构建 JAR 包
mvn clean package

# 跳过测试构建
mvn clean package -DskipTests
```

## 配置文件

- `application.yml` - 默认配置
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置

## 包结构

```
com.example.certificate/
├── config/           # 配置类
├── common/           # 通用模块
│   ├── constant/     # 常量定义
│   ├── exception/    # 异常处理
│   ├── response/     # 响应封装
│   └── util/         # 工具类
├── controller/       # REST 控制器
├── service/          # 业务服务层
│   ├── dto/          # 数据传输对象
│   └── impl/         # 服务实现
├── domain/           # 领域层
│   ├── model/        # 领域模型
│   └── repository/   # 仓库接口
├── infrastructure/   # 基础设施层
│   └── repository/   # 仓库实现
└── security/         # 安全模块
```

## 数据库配置

开发环境数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cc_bmad_opus_certificate_management
    username: root
    password: root
```

## API 文档

启动应用后访问：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

## 健康检查

- 健康端点：http://localhost:8080/api/actuator/health
- 信息端点：http://localhost:8080/api/actuator/info
- 指标端点：http://localhost:8080/api/actuator/metrics

## 日志配置

日志文件位置：`logs/certificate-management.log`

日志级别配置：
- 开发环境：DEBUG
- 生产环境：INFO

## 安全配置

- 使用 JWT 进行认证
- 密钥配置在环境变量 `JWT_SECRET`
- 默认过期时间：24小时

## 构建和部署

```bash
# 构建 JAR
mvn clean package

# 运行 JAR
java -jar target/certificate-lifecycle-management-backend-1.0.0.jar

# 指定环境运行
java -jar -Dspring.profiles.active=prod target/certificate-lifecycle-management-backend-1.0.0.jar
```