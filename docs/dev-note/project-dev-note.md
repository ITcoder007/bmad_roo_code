# 项目开发效率指南（LLM专用）

## 🔴 第0条铁律：必须使用 TDD（测试驱动开发）

### 为什么 TDD 是强制性的？
**不用 TDD = 后面所有问题的根源**。本文档中 90% 的问题都是因为没有使用 TDD 导致的。

### TDD 三步循环（严格执行）
```bash
1. 🔴 RED：先写失败的测试
   - 定义期望行为
   - 测试必须失败（确保测试有效）
   
2. 🟢 GREEN：写最少代码通过测试
   - 不要过度设计
   - 只写刚好够通过的代码
   
3. 🔵 REFACTOR：在测试保护下重构
   - 改进代码结构
   - 测试始终保持绿色
```

### TDD 执行模板
```java
// 第1步：写测试（必须失败）
@Test
void should_calculate_status_as_expiring_soon_when_30_days_left() {
    // Given
    Certificate cert = createCertificateExpiringIn(30);
    
    // When
    CertificateStatus status = cert.calculateStatus();
    
    // Then
    assertThat(status).isEqualTo(CertificateStatus.EXPIRING_SOON);
}
// 运行测试 → 🔴 失败（因为方法还不存在）

// 第2步：写最少代码
public CertificateStatus calculateStatus() {
    return CertificateStatus.EXPIRING_SOON; // 最简单的实现
}
// 运行测试 → 🟢 通过

// 第3步：添加更多测试驱动真实实现
@Test
void should_calculate_status_as_normal_when_60_days_left() {
    // 新测试强制你实现真实逻辑
}
```

### TDD 直接解决的问题
- ✅ **不会Mock目标方法**：因为先写测试，自然知道什么该Mock
- ✅ **Mock配置完整**：测试驱动会暴露所有依赖
- ✅ **测试真实有效**：红-绿循环保证测试真的在验证功能
- ✅ **设计更简洁**：测试驱动出的接口天然简单

## 🚨 最高优先级规则（必须遵守）

### 1. Mock规则：永不Mock目标方法
```java
// ❌ 致命错误
@Mock private TargetService targetService;  // 不能Mock要测试的对象

// ✅ 正确做法
@Mock private ExternalDependency dependency;  // 只Mock外部依赖
@InjectMocks private TargetService targetService;  // 被测对象用真实实例
```

### 2. 测试执行策略：分层执行
```bash
# 执行顺序（节省90%调试时间）
mvn test -Dtest=SpecificTest  # 1. 先跑单个失败的测试
mvn test -Dtest=*ServiceTest   # 2. 再跑相关层的测试
mvn test                       # 3. 最后才跑全量测试
```

### 3. Mock配置完整性检查
```java
// 分析被测方法的完整调用链
@InjectMocks ServiceImpl service;
@Mock Repository repository;  
@Mock ConfigClass config;  // 容易遗漏！

@Test void test() {
    // 必须Mock所有依赖的方法调用
    when(config.getValue()).thenReturn(30);  // 不要遗漏配置类
    when(repository.find()).thenReturn(data);
}
```

## ⚡ 快速故障排查清单

### 后端测试失败时的检查顺序
1. **PotentialStubbingProblem** → Mock参数不匹配 → 使用`anyInt()`等匹配器
2. **NullPointerException** → 缺少Mock配置 → 检查所有@Mock字段的方法调用
3. **断言失败** → 检查是否Mock了目标方法本身
4. **路径错误** → 先`pwd`确认当前目录

### 前端开发问题速查
| 症状 | 原因 | 解决方案 |
|-----|------|---------|
| 组件报`$listeners`未定义 | Vue 2语法在Vue 3中失效 | 删除，已自动合并到$attrs |
| 测试报Cannot find module '@/...' | Vitest未配置路径别名 | vitest.config.js添加resolve.alias |
| 环境变量undefined | 未使用VITE_前缀 | 改为VITE_开头的变量名 |
| Element Plus组件不生效 | 未正确导入 | 检查main.js中的注册方式 |
| 路由跳转404 | 路由路径不匹配 | 检查是否有前导斜杠 |
| Pinia状态丢失 | 页面刷新导致 | 考虑持久化插件 |

### 常见陷阱速查表
| 症状 | 原因 | 解决方案 |
|-----|------|---------|
| 测试难写/大量Mock | 没用TDD，代码先行 | 删掉代码，用TDD重写 |
| 测试永远通过 | Mock了目标方法 | 改用@InjectMocks |
| Mock参数不匹配 | 硬编码参数值 | 使用any*()匹配器 |
| 配置值为null | 忘记Mock配置类 | 添加config的Mock |
| cd命令失败 | 重复进入目录 | 先pwd再决定 |
| 时间测试不稳定 | 依赖当前时间 | 固定时间基准点 |

## 📋 开发前必查清单

### 开始任何编码前
- [ ] **TDD第一步**：先写一个失败的测试
- [ ] 确认测试真的失败了（🔴 RED）
- [ ] 写最少代码让测试通过（🟢 GREEN）

### 写测试前
- [ ] 识别所有外部依赖（Repository、Config、外部Service）
- [ ] 确认目标方法不在Mock列表中
- [ ] 准备参数匹配策略（精确匹配 vs any*()）

### 运行测试前
- [ ] 确认当前工作目录（pwd）
- [ ] 选择正确的测试范围（单个→层级→全量）
- [ ] 检查Mock配置完整性

### 测试失败后
- [ ] 先看错误类型（NPE？参数不匹配？断言失败？）
- [ ] 对照速查表快速定位
- [ ] 只修复相关代码，不要盲目全改

## 🎯 核心原则（精简版）

1. **TDD强制**：必须红-绿-重构循环，不是建议是强制
2. **测试真实性**：Mock依赖，不Mock目标
3. **执行效率**：小范围优先，逐步扩大
4. **配置完整**：所有@Mock字段的方法都要when()
5. **参数灵活**：优先用any*()匹配器，除非需要精确验证
6. **时间确定**：测试中的时间必须可控，不能随机

## 💡 效率提升技巧

### 1. 快速定位测试问题
```bash
# 只看失败的测试输出
mvn test | grep -A 10 "FAILURE\|ERROR"
```

### 2. Mock配置模板
```java
@BeforeEach
void setUp() {
    // 标准化配置Mock
    when(config.getExpiringSoonDays()).thenReturn(30);
    when(config.getBatchSize()).thenReturn(100);
    // 使用lenient()处理可能不被调用的Mock
    lenient().when(repository.count()).thenReturn(10L);
}
```

### 3. 批量修复测试
```java
// 使用 @ParameterizedTest 减少重复代码
@ParameterizedTest
@ValueSource(ints = {30, 15, 0, -5})
void testStatusCalculation(int daysUntilExpiry) {
    // 一个测试方法覆盖多个场景
}
```

## ⚠️ 项目特定注意事项

### 后端注意事项
1. **Java 8 兼容性**：不要使用Java 9+的API
2. **Spring Boot测试**：需要@SpringBootTest的测试更慢，尽量用纯单元测试
3. **MyBatis测试**：Repository层测试可能需要@MybatisTest或使用Mock

### 前端注意事项（Vue 3 + Vite）
1. **Vue 3 迁移陷阱速查**
   ```javascript
   // ❌ Vue 2 写法（会报错）
   v-on="$listeners"  // Vue 3已移除
   this.$children     // Vue 3已移除
   Vue.filter()       // Vue 3已移除
   
   // ✅ Vue 3 正确写法
   // $listeners已合并到$attrs，不需要显式传递
   // 使用provide/inject替代$children
   // 使用computed或方法替代filter
   ```

2. **测试框架配置（Vitest）**
   ```javascript
   // ❌ 错误：测试无法解析@别名
   import { something } from '@/utils/request'  // 报错：Cannot find module
   
   // ✅ 解决方案：确保vitest.config.js配置了别名
   resolve: {
     alias: {
       '@': fileURLToPath(new URL('./src', import.meta.url))
     }
   }
   ```

3. **环境变量前缀**
   ```javascript
   // ❌ 错误：环境变量无法访问
   const apiUrl = import.meta.env.API_BASE_URL  // undefined
   
   // ✅ 正确：必须以VITE_开头
   const apiUrl = import.meta.env.VITE_API_BASE_URL  // 正常访问
   ```

---

## 🔄 最近更新的坑

### 2025-08-16 Story 1.5 前端框架QA审查
- **问题1**：BaseTable组件使用了Vue 2的`$listeners`语法导致Vue 3不兼容
- **原因**：开发时未注意Vue 2到Vue 3的破坏性变更
- **教训**：前端框架升级要查阅迁移指南，特别注意已移除的API
- **解决**：Vue 3中`$listeners`已合并到`$attrs`，删除`v-on="$listeners"`即可

- **问题2**：Vitest测试无法运行，报"Cannot find module '@/utils/request'"
- **原因**：测试配置中的模块别名解析问题
- **教训**：测试框架需要独立配置路径别名，不会自动继承vite.config.js
- **解决**：在vitest.config.js中显式配置resolve.alias

### 2025-08-15 Story 1.4 QA审查
- **问题**：`CertificateStatusServiceTest`测试失败
- **原因**：没有Mock `certificateStatusConfig.getExpiringSoonDays()`
- **教训**：配置类的方法调用容易被忽略，要特别注意

---

*优化原则：让LLM在30秒内找到所需信息，而不是阅读冗长说明*