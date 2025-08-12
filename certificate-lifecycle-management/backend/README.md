# 证书管理系统 - 后端服务

基于 Spring Boot 2.7.x 构建的企业级后端服务。

## 技术栈

- **Java 8** - 编程语言
- **Spring Boot 2.7.x** - 应用框架
- **Spring Security** - 安全框架
- **JWT** - 身份认证
- **MyBatis Plus 3.5.x** - ORM框架
- **MySQL 8.0** - 关系型数据库
- **Maven 3.8.x** - 项目管理工具
- **Logback** - 日志框架

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/certificate/
│   │   │   ├── CertificateManagementApplication.java  # 应用入口
│   │   │   ├── config/         # 配置类
│   │   │   ├── common/         # 通用模块
│   │   │   │   ├── constants/  # 常量定义
│   │   │   │   ├── exception/  # 异常处理
│   │   │   │   ├── response/   # 响应封装
│   │   │   │   └── utils/      # 工具类
│   │   │   ├── controller/     # 控制器层
│   │   │   ├── service/        # 服务层
│   │   │   │   ├── dto/        # 数据传输对象
│   │   │   │   ├── vo/         # 视图对象
│   │   │   │   └── impl/       # 服务实现
│   │   │   ├── domain/         # 领域层
│   │   │   │   ├── model/      # 领域模型
│   │   │   │   ├── repository/ # 仓库接口
│   │   │   │   ├── service/    # 领域服务
│   │   │   │   └── event/      # 领域事件
│   │   │   ├── infrastructure/ # 基础设施层
│   │   │   │   ├── persistence/# 持久化实现
│   │   │   │   └── external/   # 外部服务
│   │   │   └── security/       # 安全模块
│   │   └── resources/
│   │       ├── application.yml         # 基础配置
│   │       ├── application-dev.yml     # 开发环境
│   │       ├── application-test.yml    # 测试环境
│   │       ├── application-prod.yml    # 生产环境
│   │       └── mapper/                 # MyBatis映射文件
│   └── test/                           # 测试代码
└── pom.xml                             # Maven配置
```

## 架构设计

### 分层架构

1. **Controller层** - 处理HTTP请求，参数校验
2. **Service层** - 业务逻辑处理
3. **Domain层** - 领域模型和业务规则
4. **Infrastructure层** - 基础设施实现
5. **Common层** - 通用组件和工具

### 设计原则

- **单一职责原则** - 每个类只负责一项职责
- **依赖倒置原则** - 高层模块不依赖低层模块
- **接口隔离原则** - 使用多个专门的接口
- **开闭原则** - 对扩展开放，对修改关闭

## 开发指南

### 环境要求

- JDK 8
- Maven 3.6+
- MySQL 8.0
- IDE推荐：IntelliJ IDEA

### 数据库配置

1. 创建数据库
```sql
CREATE DATABASE certificate_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置数据库连接
修改 `application-dev.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/certificate_management
    username: your_username
    password: your_password
```

### 启动应用

#### 使用Maven
```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 指定端口
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

#### 使用IDE
1. 导入Maven项目
2. 运行 `CertificateManagementApplication.java`
3. 设置启动参数：`--spring.profiles.active=dev`

### 构建打包

```bash
# 打包（跳过测试）
mvn clean package -DskipTests

# 打包（包含测试）
mvn clean package

# 生成的JAR文件位置
target/certificate-management-1.0.0-SNAPSHOT.jar
```

### 运行JAR包
```bash
java -jar target/certificate-management-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## API规范

### RESTful设计

- GET `/api/certificates` - 获取证书列表
- GET `/api/certificates/{id}` - 获取证书详情
- POST `/api/certificates` - 创建证书
- PUT `/api/certificates/{id}` - 更新证书
- DELETE `/api/certificates/{id}` - 删除证书

### 响应格式

成功响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  }
}
```

错误响应：
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

### 状态码规范

- 200 - 成功
- 400 - 请求参数错误
- 401 - 未认证
- 403 - 无权限
- 404 - 资源不存在
- 500 - 服务器内部错误

## 编码规范

### 命名规范

- **类名**：PascalCase，如 `CertificateService`
- **方法名**：camelCase，如 `getCertificateById`
- **常量**：UPPER_SNAKE_CASE，如 `MAX_RETRY_COUNT`
- **包名**：全小写，如 `com.example.certificate`
- **数据库表**：snake_case，如 `certificate_info`

### 注释规范

类注释：
```java
/**
 * 证书服务实现类
 *
 * @author 作者名
 * @date 2025-08-12
 */
```

方法注释：
```java
/**
 * 根据ID获取证书信息
 *
 * @param id 证书ID
 * @return 证书信息
 * @throws CertificateNotFoundException 证书不存在时抛出
 */
```

### 异常处理

1. 使用自定义异常
2. 统一异常处理
3. 记录异常日志
4. 返回友好错误信息

## 安全配置

### JWT配置

JWT密钥和过期时间配置：
```yaml
spring:
  security:
    jwt:
      secret: your-secret-key
      expiration: 86400000 # 24小时
```

### 跨域配置

在 `WebMvcConfig` 中配置CORS：
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

## 日志配置

日志级别配置：
```yaml
logging:
  level:
    root: INFO
    com.example.certificate: DEBUG
  file:
    name: logs/certificate-management.log
```

## 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
mvn verify
```

### 测试覆盖率
```bash
mvn jacoco:report
```

## 性能优化

1. **数据库优化**
   - 使用索引
   - 优化查询语句
   - 使用连接池

2. **缓存策略**
   - 使用Spring Cache
   - 缓存热点数据
   - 设置合理过期时间

3. **异步处理**
   - 使用@Async注解
   - 配置线程池
   - 处理耗时操作

## 监控

### 健康检查
```
GET /api/actuator/health
```

### 应用信息
```
GET /api/actuator/info
```

### 指标监控
```
GET /api/actuator/metrics
```

## 部署

### Docker部署
```dockerfile
FROM openjdk:8-jdk-alpine
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 系统要求
- 内存：最少512MB，建议2GB
- CPU：最少1核，建议2核
- 磁盘：最少10GB

## 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否启动
   - 验证连接配置
   - 检查网络连接

2. **启动失败**
   - 检查端口是否被占用
   - 查看日志文件
   - 验证配置文件

3. **内存溢出**
   - 调整JVM参数
   - 检查内存泄漏
   - 优化代码逻辑

## 相关文档

- [Spring Boot文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus文档](https://baomidou.com/)
- [Spring Security文档](https://spring.io/projects/spring-security)

---

*最后更新：2025-08-12*