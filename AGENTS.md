# XXL-TOOL 研发规范

> **定位**：XXL-TOOL 是面向 Java 开发者的基础工具类库（单 Maven 模块、以包划分能力），本文件沉淀本仓库的架构约定、API 设计、编码规范、测试与发布流程。
>
> **读者**：参与本仓库研发的工程师与 AI 编程助手。
>
> **硬约束**：新增或修改代码前先阅读本文件；同类能力以仓库现有实现为准，不另起一套写法。

## 文档导航

| 我要做的事 | 入口 |
|---|---|
| 了解仓库定位、模块划分与包结构 | 一、项目架构 |
| 拿捏实现取舍（简单 / 抽象 / 复用 / 兼容） | 二、设计原则 |
| 命名、依赖版本、注释、日志 | 三、通用约定 |
| 设计一个工具类 / 接口 / 枚举 / 构建器 | 四、API 设计规范 |
| 编写具体模块（cache、concurrent、http、json…） | 五、模块实现规范 |
| 补测试 | 六、测试规范 |
| 更新 README、官方文档、版本与发布 | 七、文档与发布 |
| 为需求建方案目录、写 plan.md | 八、需求落盘（spec） |
| 从需求到提交走完整流程 | 九、开发流程 |
| 交付前自查 | 十、验收清单 |

---

## 一、项目架构

### 1.1 仓库定位

- 单模块 `jar`：`groupId=com.xuxueli`、`artifactId=xxl-tool`。
- 面向使用者的**公共 API 库**：任何对外暴露的类、方法、枚举、异常都属于兼容性契约。
- 运行环境：JDK 17+、Maven 3+；源码与资源统一 UTF-8。
- 不依赖 Spring 运行、不依赖容器；框架能力（Spring、Servlet、POI、Gson、FreeMarker、JWT 等）全部按需引入。

### 1.2 模块地图

包的根为 `com.xxl.tool`，**一级子包即模块**，模块内聚、模块间低耦合。

```text
com.xxl.tool
├── core             基础工具：String/Date/Collection/Map/Array/Object/Bean/Class/
│                    Reflection/Type/Regex/Prop/Assert/Enum…
├── cache            本地缓存：Cache 接口 + FIFO/LFU/LRU/Unlimited/No/Reentrant 实现
├── concurrent       并发组件：MessageQueue、CyclicThread、TimeWheel、TokenBucket
├── crypto           编解码与摘要：Base64/Hex/Md5/SHA256…
├── auth             JWT 等认证能力（依赖 nimbus-jose-jwt）
├── id               ID 生成：UUID/Snowflake/Date/Random
├── io               IO/File/Csv 读写
├── http             HttpTool、CookieTool、IPTool、HttpClient 及认证扩展
├── json             GsonTool（依赖 Gson）
├── jsonrpc          轻量 JSON-RPC：Client/Server/Model
├── excel            ExcelTool + 注解（依赖 POI）
├── freemarker       FtlTool（依赖 FreeMarker）
├── captcha          图形/字符验证码
├── pipeline         流程编排：Pipeline/Handler/Executor/Config/Spring 支持
├── datastructure    BloomFilter、Trie
├── serializer       序列化抽象 + 实现 + SerializerEnum
├── response         Response/ResponseCode/PageModel 统一响应
├── error            BizException、ThrowableTool
└── deprecated       已废弃实现，仅保留兼容，禁止新增依赖
```

### 1.3 模块内部结构约定

同一模块按职责使用固定子包，已有模块保持现状，新增模块遵循：

```text
{module}/
├── XxxTool.java            模块门面 / 主入口（静态方法或构建器）
├── {Name}.java             核心模型、抽象类、枚举
├── iface/                  扩展接口（SPI），如 Cache、CacheLoader
├── impl/                   接口默认实现，如 LRUCache、JavaSerializer
├── model/                  纯数据模型，如 CacheKey、CacheObject
├── enums/                  枚举；也可平铺为 XxxType / XxxEnum
├── annotation/             注解，如 excel 的 @ExcelField、pipeline 的 @Handler
├── config/                 配置抽象 + impl/ 默认实现
└── support/                与外部框架的集成，如 SpringPipelineExecutor
```

约束：

- 一个模块只解决一类问题；跨模块复用通过 `com.xxl.tool.{module}` 的公开 API，不直接依赖对方 `impl`。
- `core` 是公共底座，禁止反向依赖 `cache`、`http`、`excel` 等上层模块。
- 模块门面优先 `XxxTool` 命名；有状态或需要链式配置时使用「静态工厂 + 实例」模式（参考 `CacheTool`、`TokenBucket`）。

---

## 二、设计原则

按优先级执行；冲突时先保证简单、职责清晰与向后兼容，再考虑复用。

> 速记：**能删就删 → 第三次再抽 → 一个变更理由 → 已有方案优先 → 不破坏使用者**。

| 原则 | 核心要求 | 落地判据 / 禁止 |
|---|---|---|
| **KISS 优先** | 先写最直接能工作的实现 | 禁止为假想需求预留扩展点、配置项、包装层 |
| **DRY，但只在必要时抽象** | 同一逻辑出现**第三次**再抽公共实现 | 两处相似但边界不同的代码可保留；禁止提前建抽象层 |
| **单一职责** | 一个类/方法只承担一个变更理由 | 工具类不掺杂业务语义；一个方法只做一件事 |
| **同类场景同一方案** | 已有工具/接口/模式能覆盖就不另起一套 | 禁止同一问题多套写法并存 |
| **向后兼容优先** | 公共 API 是契约，删除/改签名按废弃流程走 | 禁止在 patch/minor 版本静默删除或改变语义 |
| **零侵入、按需依赖** | 不把可选框架强加给使用者 | 禁止把可选依赖改成 `compile` 传递依赖 |

补充约束：

- 抽象前先回答：是否已重复第三次？现有工具是否已覆盖？现在是否真的需要？三个答案不成立就不抽象。
- 不为一次性逻辑建抽象类；不新增「仅转发」的工具方法。
- 修复问题优先改现有实现，不平行复制一份「新版本」；确需重写时放入 `deprecated`，保留旧实现一个周期。
- 新增能力先查 JDK 17 与已有依赖是否已覆盖（如 `Objects`、`java.time`、`java.util.concurrent`），避免重复造轮子。

---

## 三、通用约定

### 3.1 语言与命名

- 沟通与注释使用中文；标识符使用英文，表达真实语义，禁止拼音与无意义单字母。
- 类/接口/枚举：大驼峰（`StringTool`、`CacheLoader`）；方法/变量：小驼峰；常量：全大写下划线（`INDEX_NOT_FOUND`）。
- 工具类统一 `XxxTool`；接口 `Xxx` / `XxxProvider` / `XxxLoader`；实现 `XxxCache` / `XxxSerializer`；枚举 `XxxType` / `XxxStatus` / `XxxEnum`。
- 布尔字段与方法不用 `isXxx` 前缀做字段名（避免与 getter 冲突）；集合返回值用复数（`handlerList`）。
- 包名全小写、单数模块名（`cache`、`serializer`）；避免出现 `util`、`common` 这类无边界包。

### 3.2 依赖与版本

- **版本一律固定**：`pom.xml` 中依赖 `<version>` 与 `<properties>` 版本属性写精确版本；禁止 `LATEST`、`RELEASE`、区间版本。
- **可选能力依赖使用 `provided`**：与现有 `gson`、`poi-ooxml`、`spring-context`、`jakarta.servlet-api`、`freemarker`、`nimbus-jose-jwt` 保持一致，避免传递给使用者。
- 基础依赖仅 `slf4j-api`（+ 测试用 `junit-jupiter`、`slf4j-reload4j`）；新增基础依赖必须说明必要性。
- 新增可选依赖时必须同步：模块 Javadoc / README / 官方文档中的「依赖说明」，并保证未引入该依赖时**类加载不报错**（不要在类初始化阶段强依赖可选类型）。
- 不引入 Lombok、反射魔法、字节码增强、运行时代理等会改变使用者构建体验的依赖。
- 升级依赖：优先 patch/minor，验证编译 + 相关模块测试；保留原大版本兼容性说明。禁止无理由批量升级。

### 3.3 注释与 Javadoc

对外 API 必须有 Javadoc；注释解释「做什么 / 为什么」，不复述代码。

| 层级 | 要求 |
|---|---|
| 文件/类型 | 类、接口、枚举使用 Javadoc：功能描述 + 空行 + `@author 作者 yyyy-MM-dd` |
| 方法 | `public` / `protected` 方法使用 Javadoc；参数、返回值、异常按需补 `@param`、`@return`、`@throws`；简单 getter/setter 可省略 |
| 字段 | 非显而易见的字段用 Javadoc，置于字段上方；`serialVersionUID` 可保留 |
| 行为契约 | 空值语义、线程安全、超时单位、边界行为必须写清 |
| 示例 | 关键行为用 `<pre>` 给出输入输出示例（参考 `StringTool.isEmpty`） |
| 分支 | `//` 置于分支/提前返回上方，说明原因 |

```java
/**
 * 字符串工具
 *
 * @author xuxueli 2020-04-16
 */
public class StringTool {

    /**
     * 是否为空：{@code null} 与 {@code ""} 返回 {@code true}，空白字符串返回 {@code false}
     *
     * <pre>
     * StringTool.isEmpty(null)  = true
     * StringTool.isEmpty("")    = true
     * StringTool.isEmpty(" ")   = false
     * </pre>
     *
     * @param str 待校验字符串，可为 {@code null}
     * @return 为空返回 {@code true}
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
```

禁止：无意义注释（`// 返回结果`）、注释掉的死代码、`TODO` 长期滞留（要么建 Issue，要么删除）、与实现矛盾的注释。

### 3.4 日志

- 统一使用 `org.slf4j.Logger` / `LoggerFactory`，禁止 `System.out`、`printStackTrace()`。
- 使用占位符，禁止字符串拼接：

```java
logger.debug("PipelineExecutor execute fail, pipeline[{}] is null", pipelineName);
```

- 级别：`debug` 记录内部流程与可恢复异常；`info` 仅记录生命周期关键节点；`warn`/`error` 必须可行动，禁止在循环中刷屏。
- 日志不得包含密码、密钥、Token、完整身份证/手机号等敏感信息。
- 库代码默认安静：正常路径不打 `info`；需要排查时通过 `debug` 输出。

---

## 四、API 设计规范

### 4.1 工具类形态

- **无状态工具**：全部 `static` 方法，类可 `final`、构造器私有（存量类可保持现状，新增类遵循）。
- **有状态组件**：提供静态工厂（`newLRUCache()`、`create()`）+ 实例方法；配置用链式方法，构建后就绪。
- **可扩展能力**：先定义最小接口（`Cache`、`Serializer`、`HttpInterceptor`），再提供默认实现；禁止为单一实现造接口。
- **注解**：只用于声明式映射（Excel 字段、Pipeline Handler），注解保持无副作用，处理逻辑集中在工具类。

```java
/**
 * 缓存工具
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author xuxueli 2018-01-22
 */
public class CacheTool<K, V> {

    /** 创建默认 LRU 缓存 */
    public static <K, V> CacheTool<K, V> newCache() {
        return newLRUCache();
    }
}
```

### 4.2 空值与参数契约

- 每个公开方法的空值语义必须在 Javadoc 中写清；同类方法保持一致：
  - 查询/校验类返回布尔，通常对 `null` 安全（`isEmpty(null) == true`）；
  - 集合/数组工具返回空集合而非 `null`；
  - 查找类方法未命中返回 `null`，或在方法名中体现（`getIfPresent`）。
- 参数非法时优先抛 `IllegalArgumentException`（可用 `AssertTool`）；不返回 `null` 表示失败，也不静默吞掉参数错误。
- 不在工具方法中修改入参集合/数组，除非方法名明确表达（`sort`、`addAll`）；需要修改时文档写明。

### 4.3 异常

- 通用业务异常使用 `BizException`（`RuntimeException`）；工具层参数错误用 `IllegalArgumentException` / `IllegalStateException`。
- 受检异常尽量在边界转换；IO/网络工具按需声明或包装为运行时异常，并在 Javadoc 说明。
- 捕获异常必须有处理：记录日志、降级、重新抛出三选一；禁止空 `catch`。
- 异常信息包含可定位上下文（key、参数名、长度），但不得泄露敏感数据。

### 4.4 线程安全

- 无状态工具类天然线程安全；有状态组件必须在类 Javadoc 标注「线程安全 / 非线程安全」及并发语义。
- 共享可变状态使用 `ConcurrentHashMap`、`Atomic*`、`volatile`、`synchronized` 等显式手段；优先无锁与分段锁（参考 `cache`、`MessageQueue`）。
- 静态可变字段原则上禁止；确需全局注册表时必须并发安全并提供清理入口。
- 内部线程：设置名称、按需 `daemon`、避免线程泄漏；`start/stop` 必须幂等；关闭时释放资源。

### 4.5 性能与资源

- 热路径避免：重复创建 `Pattern`（用 `static final` 缓存）、无意义装箱、循环内字符串拼接、频繁 Stream 链。
- 大文件/大集合使用流式或分批处理（参考 `FileTool`、`CsvTool`），避免一次性读入内存。
- 所有流、连接、`Workbook`、`ExecutorService` 使用 try-with-resources 或显式关闭。
- 有容量语义的容器必须支持上限或淘汰策略，禁止无界增长。

### 4.6 安全

- 摘要算法只用于完整性校验，不得宣称可用于密码存储；密码场景使用专用 KDF（bcrypt/PBKDF2/Argon2）。
- 禁止硬编码密钥、账号、Token；示例中使用占位符。
- JWT 校验必须验证签名、过期时间与算法，禁止 `none` 算法与关闭校验。
- HTTP/文件工具需防范 SSRF、路径穿越与任意文件覆盖；对外部输入做白名单或规范化校验。
- `JavaSerializer` 等反序列化能力不得用于不可信数据，文档必须提示风险。
- 不在日志、异常消息、`toString()` 中输出敏感信息。

### 4.7 兼容性与废弃

- 公共类/方法签名、返回语义、枚举常量、SPI 接口均视为兼容契约：
  - 新增能力用**重载 / 新方法**，不修改既有签名；
  - 不改变既有方法的空值、异常、线程安全语义；
  - `Serializable` 类显式声明 `serialVersionUID`。
- 废弃流程：
  1. 方法或类加 `@Deprecated`，Javadoc 加 `@deprecated` 说明替代方案与起始版本；
  2. 复杂旧实现迁入 `com.xxl.tool.deprecated.{module}`，至少保留一个大版本；
  3. 在官方文档「版本更新日志」标注废弃与移除计划；
  4. 未确认使用者迁移前，禁止直接删除。
- `deprecated` 包只做兼容维护：不新增能力、不新增依赖、不扩散引用。

---

## 五、模块实现规范

### 5.1 通用要求

- 新模块放入 `com.xxl.tool.{module}`，门面类 `XxxTool`；需要扩展时按 1.3 建 `iface/impl/model/…`。
- 模块自包含：异常、模型、常量放在本模块内，不污染 `core`。
- 依赖可选框架时，公开 API 尽量使用 JDK 类型（`byte[]`、`String`、`Map`），避免把第三方类型暴露到签名中。

### 5.2 core

- 纯 JDK 实现，禁止引入业务语义与第三方依赖。
- 方法按功能分区，用 `// ---------------------- xxx ----------------------` 分隔（与现有代码一致）。
- 对 `null`、空串、越界、负索引给出确定行为并写进 Javadoc；参考 `StringTool.substring`、`CollectionTool`。

### 5.3 cache

- 通过 `Cache` 接口扩展；新增淘汰策略实现 `Cache` 并接入 `CacheType` 与 `CacheTool` 工厂。
- 明确 `timeout` 单位（毫秒）、`expireAfterWrite` / `expireAfterAccess` 语义、`capacity=0` 表示无限制。
- 定时清理线程必须可关闭；缓存统计与 `prune()` 行为保持一致。

### 5.4 concurrent

- 组件需说明吞吐、阻塞、公平性、关闭语义；`MessageQueue` 等有界结构必须处理满/空。
- 后台线程统一命名，异常不得导致线程静默退出（记录日志并按策略继续或停止）。
- 并发测试至少覆盖：多线程读写、启停幂等、边界容量。

### 5.5 http / json / excel / freemarker / auth

- 这些模块依赖可选库：未引入依赖时，仅在使用对应 API 时报错，且错误信息明确提示所需依赖。
- 不修改全局静态配置（如默认超时、默认 ObjectMapper）影响其他调用方；需要时通过实例或参数传入。
- `HttpTool` 的请求构造优先复用 `HttpRequest` / `HttpClientService` / `AuthProvider`，不重复实现认证与重试。
- `ExcelTool` 的注解与模型映射保持向后兼容；新增注解属性必须有默认值。

### 5.6 pipeline

- 扩展点：`PipelineHandler`、`PipelineConfigLoader`、`PipelineContext`；Spring 集成放 `support`。
- Handler 执行顺序、异常传播、上下文共享方式必须写进 Javadoc。
- 执行器注册表并发安全；同名 Pipeline 覆盖策略明确。

### 5.7 response / error

- 统一响应结构固定为 `Response{code, msg, data}`，成功码使用 `ResponseCode.SUCCESS`；禁止另造响应体。
- 分页统一 `PageModel`；新增状态码在 `ResponseCode` 中集中定义，不复用魔法数字。
- `BizException` 保持轻量，不绑定 Web 框架。

### 5.8 io

- 文件操作必须明确覆盖/追加、字符集、父目录创建行为；路径拼接使用 `File`/`Path`，禁止字符串硬拼分隔符。
- 大文件读写走流式 API；关闭责任清晰（工具方法内部关闭，或文档声明由调用方关闭）。

---

## 六、测试规范

- 框架：JUnit 5（`org.junit.jupiter.api.Test`）；测试代码放 `src/test/java/com/xxl/tool/test/{module}/`，类名 `XxxToolTest`。
- 测试方法名表达场景：`isEmptyTest`、`substringTest`、`multiThreadPutTest`；一个方法聚焦一个行为。
- 新增/修改功能必须**包含断言**（`org.junit.jupiter.api.Assertions`），不得只打印日志靠人工判断；存量纯日志测试可逐步迁移。
- 必测边界：`null`、空串/空集合、0/负数、最大长度、越界、异常分支、并发场景。
- 依赖可选库的模块，测试直接使用 `provided` 依赖（test classpath 可见）；不引入新的测试专用重量级依赖。
- 本地执行（仓库默认跳过测试，需显式开启）：

```bash
mvn -B -Dmaven.test.skip=false test
# 只跑某模块
mvn -B -Dmaven.test.skip=false -Dtest='CacheToolTest' test
```

- CI 执行 `mvn -B package`；提交前本地至少完成编译 + 相关模块测试。
- 不提交只用于本地调试的临时测试；不修改无关测试来「让构建变绿」。

---

## 七、文档与发布

### 7.1 文档同步（强制）

新增/调整能力时同步以下位置，保持单一事实来源：

| 变更 | 必须同步 |
|---|---|
| 新增工具类 / 模块 | `README.md` 的 Modules 与 Tool List；`doc/XXL-TOOL官方文档.md` 对应章节 |
| 新增可选依赖 | 模块文档「依赖说明」+ `pom.xml`（`provided` + 精确版本） |
| 行为变更 / 废弃 | 官方文档「版本更新日志」 |
| 版本号 | `pom.xml` 的 `<version>` |

- README 与官方文档的模块描述保持一致的措辞与顺序；不产生两套互相矛盾的说明。
- 文档示例代码必须可编译或明确标注伪代码；不在文档中留下过时 API。

### 7.2 版本与发布

- 开发中版本使用 `x.y.z-SNAPSHOT`；发布前去 `-SNAPSHOT`。
- 发布流程：更新版本号 → 更新更新日志 → 编译 + 测试 → 生成 source/javadoc → GPG 签名 → 发布 Maven Central。
- 更新日志使用统一前缀：`【新增】`、`【优化】`、`【修复】`、`【升级】`、`【废弃】`，末条注明版本与日期。
- 破坏性变更必须提升主版本或明确在日志中标注迁移方式，禁止隐性破坏。

---

## 八、需求落盘（spec）

### 8.1 定位与权威边界

`xxl-tool-spec/` 用于沉淀**单个需求**的方案、API 草案、验证数据与决策记录，随代码入库，作为评审与追溯依据。

| 位置 | 职责 | 不写什么 |
|---|---|---|
| `AGENTS.md`（本文件） | 通用架构、设计原则、API/编码/测试/发布规范 | 单个需求的方案与结论 |
| `xxl-tool-spec/{yyyyMMdd}-{module}/` | 单个需求的目标、API 草案、取舍、兼容性、验证结果 | 通用规范（引用本文件即可） |
| `README.md` / `doc/XXL-TOOL官方文档.md` | 面向使用者的功能说明与示例 | 内部实现细节、未定稿方案 |
| 源码 Javadoc | 最终 API 契约 | 需求背景与演进过程 |

- 需求存在 spec 时，实现前先读 spec；实现结果与 spec 不一致时，以确认后的最新结论为准并回填 spec。
- spec 是历史记录，交付后保留，不因代码合入而删除；后续大改在原目录追加变更记录，或新建目录并互相引用。

### 8.2 目录与命名

```text
xxl-tool-spec/
└── {yyyyMMdd}-{module}[-{feature}]/
    ├── plan.md          必需：需求、API、实现、兼容性、验证、文档
    ├── api.md           可选：公共 API 草案（签名、Javadoc 契约、示例）
    ├── benchmark.md     可选：性能对比 / 压测方法、环境与数据
    └── notes.md         可选：调研结论、备选方案、风险与待确认项
```

- `{module}` 与包名一致（`core`、`cache`、`http`、`pipeline`…）；`{feature}` 用小写中划线（`http-retry`）。
- 同日多个需求必须带 `{feature}` 区分，禁止合并成一个目录。
- 需求名与最终 API/模块命名保持一致；改名时同步目录名，避免历史目录指向错误能力。
- 只落盘与本次需求相关的内容，不建 `common/`、`misc/` 等无边界目录。

### 8.3 `plan.md` 骨架

```markdown
# {需求名}（{module}/{feature}）

> 需求目录：`xxl-tool-spec/{yyyyMMdd}-{module}-{feature}/` | 日期：{yyyy-MM-dd} | 状态：草案 / 开发中 / 已交付

## 一、需求与现状

| 项 | 结论 |
|---|---|
| 使用场景 | 谁在什么场景下使用，解决什么问题 |
| 现有能力与查重 | 已有 `XxxTool` / JDK / 依赖是否覆盖；为何不能直接复用 |
| 目标 API | `com.xxl.tool.{module}.XxxTool#method`，一句话描述 |
| 是否新增依赖 | 无 / `{groupId}:{artifactId}`，作用域 `provided`，版本 `{x.y.z}` |
| 兼容性影响 | 纯新增 / 重载新增 / 行为修正 / 废弃替代 |
| 验证范围 | 单测 / 并发测试 / 基准测试 / 示例可运行 |

## 二、API 设计

- 类与方法签名（含泛型、异常声明）
- 空值语义、返回约定、超时/容量单位
- 线程安全语义与生命周期（是否需要 close/stop）
- 调用示例（可编译优先）

## 三、实现方案

- 落位包与文件清单（门面、接口、实现、模型）
- 关键数据结构 / 算法 / 并发策略
- 可选依赖隔离方式（未引入依赖时的行为）
- 备选方案与不采用原因（只记录真实取舍）

## 四、兼容性与风险

- 对既有公共 API 的影响；是否需要 `@Deprecated`
- 依赖升级、安全、性能回退风险
- 回滚方式与影响范围

## 五、测试与验证

- 用例清单：正常 / 空值 / 边界 / 异常 / 并发
- 执行命令与结果
- 性能类需求：环境、数据集、预热、指标与对比结论（见 `benchmark.md`）

## 六、文档与发布

- README（Modules / Tool List）更新点
- 官方文档章节与「依赖说明」更新点
- 版本更新日志条目、目标版本号

## 七、变更记录

| 日期 | 变更 | 原因 |
|---|---|---|
| {yyyy-MM-dd} | 初稿 / API 调整 / 验证结果回填 | … |
```

### 8.4 编写要求

- **先落盘、后编码**：非平凡需求（新增模块、公共 API、依赖或行为变更）先建目录与 `plan.md`，确认 API 与兼容性结论后再实现。
- **结论优先**：写清「选了什么、为什么、影响什么」，不堆砌过程性描述；未确认项标注「待确认」并列入 `notes.md`。
- **契约可执行**：API 草案尽量给出真实签名与示例；性能结论必须可复现（JDK 版本、机器、数据规模、预热轮次、对比基线）。
- **不重复规范**：通用约束引用本文件章节号，不在 spec 中复制粘贴；spec 只写本次需求的特殊结论。
- **及时回填**：实现中调整 API、依赖或行为时同步更新 `plan.md`；交付前保证 spec 与代码、README、官方文档一致。
- **不写假数据**：未执行的测试、未测量的性能、未确认的兼容性不得写成结论；标注实际验证范围。
- **AI 助手**：任务已有 spec 时先读并遵循；缺失且任务非平凡时，先按 8.3 建骨架并向用户确认关键结论，不擅自跳过落盘。

### 8.5 生命周期

1. **建立**：需求确认阶段创建目录与 `plan.md`，完成第一章与 API 草案。
2. **评审**：重点检查查重结论、API 契约、兼容性、依赖与安全影响；结论回填 spec。
3. **开发**：按 spec 实现；偏离时先更新 spec 再改代码。
4. **交付**：回填测试结果、文档同步与变更记录，状态改为「已交付」。
5. **维护**：后续调整在原目录追加变更记录；破坏性替代需新建 spec 并引用旧目录。

---

## 九、开发流程

1. **需求确认**：明确使用场景、输入输出、空值/异常/线程安全语义、是否引入依赖；确认是否属于已有模块。
2. **需求落盘**：非平凡需求按第八章建立 `xxl-tool-spec/{yyyyMMdd}-{module}/` 与 `plan.md`，回填查重结论、API 草案与兼容性判断。
3. **方案落位**：确定包路径、门面类、接口与实现；新模块按 1.3 建目录；先查重，避免与现有工具重复。
4. **实现**：遵循第四、五章；公共 API 先写 Javadoc 契约，再写实现；偏离 spec 时先更新 spec。
5. **测试**：按第六章补断言与边界/并发用例，本地执行通过；结果回填 spec。
6. **文档**：按 7.1 同步 README、官方文档、依赖说明与更新日志；回填 spec 的文档章节。
7. **自查**：逐项勾选第十章清单。
8. **提交**：只提交任务相关文件，不提交 `target/`、IDE 配置、临时文件；提交信息简洁，遵循仓库风格（`feat: ...` / `fix: ...` / `docs: ...` / 中文简述均可，同一 PR 保持一致）。

AI 助手补充约定：

- 改动前先阅读同模块现有实现与测试，保持风格一致；不做与任务无关的重构与格式化。
- 不擅自新增/升级依赖；确需新增时先说明用途、作用域（`provided`）与文档同步点。
- 不删除 `deprecated` 包内容，不修改公共 API 语义；无法确认兼容性时先询问。
- 完成后必须给出：改动文件、验证命令与结果、文档同步情况。

---

## 十、验收清单

**spec**

- [ ] 非平凡需求已在 `xxl-tool-spec/{yyyyMMdd}-{module}/` 落盘 `plan.md`
- [ ] 查重结论、API 契约、兼容性、依赖与验证结果已回填，且与代码一致
- [ ] 状态、变更记录、相关文档引用完整

**设计**

- [ ] 能力归属正确，未与现有工具重复；新抽象满足「第三次」原则
- [ ] 公共 API 命名符合约定，Javadoc 写清空值、异常、线程安全、单位与边界
- [ ] 未破坏既有签名/语义；废弃项已按流程标注

**实现**

- [ ] 无 `System.out` / `printStackTrace` / 空 `catch` / 死代码 / 无意义注释
- [ ] 日志使用占位符，无敏感信息
- [ ] 资源正确关闭；并发组件线程可停止、命名明确
- [ ] 无静态可变状态；有状态组件已标注线程安全语义
- [ ] 热路径无明显性能问题（正则缓存、避免无界增长）
- [ ] 安全默认值正确（摘要用途、JWT 校验、路径与 SSRF 防护）

**依赖与构建**

- [ ] 可选依赖为 `provided` 且版本精确；未引入时不影响其他类加载
- [ ] `mvn -B -Dmaven.test.skip=false test` 通过；`mvn -B package` 通过
- [ ] 未提交 `target/` 等构建产物

**测试**

- [ ] 新增/修改逻辑有断言；覆盖 `null`、空值、边界、异常
- [ ] 并发/资源类组件有多线程或启停用例

**文档**

- [ ] README（Modules / Tool List）已同步
- [ ] 官方文档对应章节与「依赖说明」已同步
- [ ] 行为变更、废弃、依赖升级已写入版本更新日志
