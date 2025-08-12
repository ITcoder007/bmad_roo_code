# Technical Assumptions

## Repository Structure: Monorepo

采用Monorepo结构，将前端和后端代码放在同一个代码仓库中管理。这种结构便于统一版本控制、依赖管理和CI/CD流程，特别适合中小型团队开发。代码组织将遵循领域驱动设计(DDD)原则，分为以下主要目录：
- `frontend/`: Vue前端应用代码
- `backend/`: Spring Boot后端应用代码，使用Maven进行项目管理
- `shared/`: 共享代码和工具库
- `docs/`: 项目文档
- `docker/`: Docker配置文件
- `scripts/`: 构建和部署脚本

## Service Architecture

采用单体应用架构，使用Spring Boot框架构建，基于领域驱动设计(DDD)模式组织代码。后端采用JDK 8作为开发环境，使用MyBatis Plus作为ORM框架，MySQL作为数据库。这种架构适合MVP阶段的快速开发和部署，同时为未来的微服务迁移预留可能性。系统将包含以下核心服务模块：
- **证书管理服务**：负责证书信息的CRUD操作
- **监控服务**：负责定期检查证书到期时间
- **预警服务**：负责发送证书过期预警通知
- **用户界面服务**：提供Web界面和API接口

系统将采用DDD分层架构，包括：
- **表现层**：处理HTTP请求和响应
- **应用层**：实现业务用例和协调领域对象
- **领域层**：包含核心业务逻辑和领域模型
- **基础设施层**：提供技术支持和外部集成，包括MyBatis Plus的数据访问实现

## Testing Requirements

采用单元测试和集成测试相结合的测试策略，确保系统的稳定性和可靠性。测试要求包括：
- **单元测试覆盖率**：核心业务逻辑的单元测试覆盖率达到70%以上
- **集成测试**：关键业务流程的集成测试，包括证书管理、监控和预警功能
- **API测试**：所有REST API的测试，确保接口正确性和稳定性
- **UI测试**：关键用户界面的端到端测试，确保用户体验
- **性能测试**：系统性能测试，确保满足非功能性需求中的性能指标

测试将使用以下工具和框架：
- **JUnit**: Java单元测试框架
- **Mockito**: 模拟对象框架
- **Spring Test**: Spring Boot测试支持
- **MyBatis Test**: MyBatis Plus测试支持
- **Maven Surefire Plugin**: Maven测试插件
- **Jest**: JavaScript测试框架
- **Vue Test Utils**: Vue组件测试工具
- **Selenium**: Web UI自动化测试

## Additional Technical Assumptions and Requests

- **后端技术栈**：采用JDK 8作为开发环境，Spring Boot作为应用框架，MyBatis Plus作为ORM框架，遵循DDD领域驱动设计架构
- **项目管理**：使用Maven作为项目管理和构建工具，统一管理依赖和项目生命周期
- **数据库设计**：使用MySQL作为主数据库，MyBatis Plus作为数据访问层，设计规范化的数据表结构存储证书信息和系统配置
- **前端技术栈**：采用Vue.js作为前端框架，使用Vue Router进行路由管理，简化状态管理
- **缓存策略**：MVP阶段暂不使用缓存，后续可根据性能需求添加Redis缓存层
- **消息队列**：MVP阶段使用简单的定时任务，暂不使用消息队列，后续可根据需要添加RabbitMQ
- **日志管理**：MVP阶段采用简单的文件日志记录，后续可根据需要扩展为ELK Stack
- **监控和告警**：MVP阶段暂不集成外部监控系统，使用基本的系统日志记录
- **安全措施**：采用HTTPS加密通信，使用JWT进行API认证，敏感数据加密存储
- **部署方式**：MVP阶段使用简单的JAR包部署，后续可扩展为Docker容器化部署
- **CI/CD流程**：MVP阶段使用基本的构建脚本，后续可扩展为完整的CI/CD流程
- **开发环境**：统一使用IntelliJ IDEA作为开发IDE，遵循统一的代码规范和格式
- **文档要求**：MVP阶段使用基本的Markdown编写技术文档，后续可添加Swagger生成API文档
- **代码质量**：MVP阶段使用基本的代码规范和单元测试，后续可添加SonarQube进行代码质量检查