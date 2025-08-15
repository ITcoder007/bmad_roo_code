# 项目开发避坑指南

## 核心问题和解决方案

### 问题1：单测质量可疑，把目标方法Mock掉了
**具体场景**：用户质疑"你写的单元测试是否真的有效，是不是把目标方法mock后糊弄人的？"  
**问题根因**：把要测试的目标方法给Mock掉了，测试永远不会执行真实的业务逻辑  
**解决方案**：确保目标方法不被Mock，只Mock外部依赖

**具体做法**：
```java
// ❌ 错误做法：把目标方法Mock了
@Mock private CertificateStatusService certificateStatusService;
@Test void testCalculateStatus() {
    when(certificateStatusService.calculateStatus(cert)).thenReturn(CertificateStatus.NORMAL);
    // 这样永远测不到真实的calculateStatus逻辑
}

// ✅ 正确做法：只Mock外部依赖，不Mock目标方法
@Mock private CertificateRepository certificateRepository; // Mock外部依赖
@InjectMocks private CertificateStatusServiceImpl certificateStatusService; // 真实对象
@Test void testCalculateStatus() {
    // 真实调用calculateStatus方法，会执行真实的业务逻辑
    CertificateStatus result = certificateStatusService.calculateStatus(cert);
}
```

---

### 问题2：测试失败时盲目全量跑测试
**具体场景**：代码改动后直接`mvn test`，10分钟后发现是无关的集成测试依赖问题  
**问题根因**：没有分层测试策略，浪费时间在不相关的测试上  
**解决方案**：按测试类型分层执行，快速定位问题

**具体做法**：
- 先跑核心业务逻辑测试（纯单元测试）
- 再跑需要Spring上下文的服务测试
- 最后跑需要数据库/外部依赖的集成测试
- 哪层失败就在哪层解决问题

---

### 问题3：时间边界测试的精度问题处理不当
**具体场景**：测试`getDaysUntilExpiry()`时，有时返回30天，有时29天，导致测试不稳定  
**问题根因**：测试执行时间跨越了天的边界，或者毫秒级计算精度问题  
**错误做法**：用`isBetween(29L, 30L)`允许误差范围

**正确做法**：
- 在测试中固定时间基准点，不要用`new Date()`
- 使用确定的时间差计算，避免"现在"的不确定性
- 如果必须用当前时间，应该控制测试数据让结果确定

```java
// ❌ 不稳定的做法
Certificate cert = createCertificate(30); // 30天后过期
long days = cert.getDaysUntilExpiry(); // 可能是29或30
assertThat(days).isBetween(29L, 30L); // 这是在掩盖问题

// ✅ 正确的做法  
Date fixedNow = new Date();
Date expiry = new Date(fixedNow.getTime() + 30 * 24 * 60 * 60 * 1000L);
Certificate cert = Certificate.builder().expiryDate(expiry).build();
long days = cert.getDaysUntilExpiry(fixedNow); // 传入固定基准时间
assertThat(days).isEqualTo(30L); // 结果应该是确定的
```

---

### 问题4：不用TDD导致测试质量差和返工多 ⭐ 核心问题
**具体场景**：
- 写完功能发现测试很难写，只能大量Mock来"凑"测试
- 测试通过了但实际功能有bug，说明测试没测到关键逻辑
- 需求不明确时直接开始coding，后面推倒重来

**问题根因**：代码优先，测试补充的开发方式根本就是错的！  
**解决方案**：必须严格按照TDD红绿重构循环开发，这不是选项而是必须

## TDD的核心价值（为什么必须用TDD？）

**1. 避免Mock滥用**
- TDD先写测试，会自然设计出可测试的代码结构
- 后补测试往往因为代码难测试而大量Mock，包括Mock目标方法

**2. 需求澄清**
- 写测试前必须想清楚"期望什么结果"
- 避免需求模糊就开始coding导致的返工

**3. 设计驱动**
- 测试先行会倒逼你设计出简单、可测试的API
- 代码先行往往导致复杂、难测试的设计

**TDD标准流程（必须严格执行）**：
1. **🔴 红色阶段**：先写一个**失败**的测试，明确需求和接口设计
2. **🟢 绿色阶段**：写**最少**的代码让测试通过（不要过度设计）
3. **🔵 重构阶段**：在测试保护下改进代码结构
4. **🔁 重复循环**：每个新功能都必须走这个流程

**具体实施（非常重要）**：
- **测试必须先失败**：确保测试真的在验证功能，而不是永远通过的假测试
- **最少代码原则**：不要一次写太多，只写刚好通过测试的代码
- **重构时测试保护**：任何重构都不能破坏现有测试
- **一次只做一件事**：一个测试用例只验证一个场景
- **测试就是活文档**：测试用例要清晰表达业务需求

**TDD的强制性**：
- 不是"建议使用"，而是**必须使用**
- 违反TDD往往导致前面3个问题（Mock滥用、测试策略混乱、时间处理不当）
- TDD是代码质量的根本保障，不是可选的"最佳实践"

---

## 核心原则

**测试质量原则**：只Mock外部依赖，绝不Mock目标方法，否则测试就是假的  
**开发效率原则**：分层测试快速定位问题，不要一上来就跑全量  
**时间处理原则**：测试中的时间计算必须是确定的，不能依赖执行时的随机性  
**TDD强制原则**：必须严格按红绿重构循环开发，这是代码质量的生命线

---

### 问题5：Maven测试执行时的工作目录问题
**具体场景**：在backend目录下执行`cd backend && mvn test`失败  
**问题根因**：已经在backend目录中，再次cd backend导致路径错误  
**解决方案**：执行命令前先确认当前工作目录

**具体做法**：
```bash
# ❌ 错误：不检查当前目录就cd
cd backend && mvn test  # 如果已在backend目录会失败

# ✅ 正确：先检查当前目录
pwd  # 先确认当前位置
mvn test -Dtest=TestClass  # 根据实际位置决定是否需要cd
```

---

### 问题7：Mockito严格模式导致的测试失败
**具体场景**：Mock配置的参数与实际调用不匹配时，Mockito抛出PotentialStubbingProblem  
**问题根因**：Mockito的严格模式会检查所有stub是否被正确使用  
**解决方案**：确保Mock配置的参数与实际调用完全匹配

**具体表现**：
```java
// ❌ 错误：Mock参数不匹配
when(repository.selectAllPaged(1, 100)).thenReturn(data);
// 但实际调用是 selectAllPaged(1, 50) - 参数不匹配！

// ✅ 正确做法1：精确匹配
when(repository.selectAllPaged(1, 50)).thenReturn(data);

// ✅ 正确做法2：使用参数匹配器
when(repository.selectAllPaged(anyInt(), anyInt())).thenReturn(data);
```

---

## 新增核心原则

**Mock完整性原则**：测试前必须分析完整调用链，确保所有外部依赖都有Mock配置  
**目录感知原则**：执行shell命令前先pwd确认当前位置，避免路径错误  
**Mock精确性原则**：Mock配置的参数必须与实际调用匹配，或使用参数匹配器  

---

## QA审查要点清单（基于Story 1.4复盘）

### 测试审查重点
1. **Mock正确性**：确认没有Mock目标方法本身
2. **Mock完整性**：检查所有依赖是否都有Mock配置
3. **参数匹配性**：验证Mock参数与实际调用是否一致
4. **测试独立性**：每个测试应该独立，不依赖其他测试的执行顺序

### 代码审查重点
1. **DDD原则遵循**：领域逻辑在领域层，服务层只做编排
2. **配置管理规范**：使用Spring Boot配置类，支持环境变量覆盖
3. **异常处理完整**：有重试机制和错误日志记录
4. **性能考虑充分**：批量操作使用分页，避免内存溢出

### 效率提升建议
1. **先跑相关测试**：不要直接mvn test，先跑特定测试类
2. **快速验证修复**：修复后只跑失败的测试，验证通过再跑全量
3. **利用测试输出**：仔细看测试错误信息，通常会指出具体问题

---

*最后更新：2025-08-15 | 来源：Story 1.4 QA审查复盘*