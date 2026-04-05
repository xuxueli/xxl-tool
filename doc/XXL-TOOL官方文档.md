## 《Java工具类库XXL-TOOL》

[![Actions Status](https://github.com/xuxueli/xxl-tool/workflows/Java%20CI/badge.svg)](https://github.com/xuxueli/xxl-tool/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.xuxueli/xxl-tool)](https://central.sonatype.com/artifact/com.xuxueli/xxl-tool/)
[![GitHub release](https://img.shields.io/github/release/xuxueli/xxl-tool.svg)](https://github.com/xuxueli/xxl-tool/releases)
[![GitHub stars](https://img.shields.io/github/stars/xuxueli/xxl-tool)](https://github.com/xuxueli/xxl-tool/)
![License](https://img.shields.io/github/license/xuxueli/xxl-tool.svg)
[![donate](https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square)](https://www.xuxueli.com/page/donate.html)

[TOCM]

[TOC]

## 一、简介

### 1.1 概述
XXL-TOOL 是一个Java工具类库，致力于让Java开发更高效。包含 “日期、集合、字符串、IO、缓存、并发、Excel、Emoji、Response、Pipeline、Http、Json、JsonRpc、Encrypt、Auth、ID、Serializer、验证码、限流器、BloomFilter...” 等数十个模块。

### 1.2 组件列表
| 模块                 | 说明
|--------------------| ---------------
| Core模块             | 包含 集合、缓存、日期、反射、断言、正则……等基础工具。
| Cache模块            | 一个高性能的 Java 缓存工具，支持多种缓存类型（FIFO、LFU、LRU等）、锁分桶优化、缓存过期策略（写后过期、访问后过期...）、缓存定时清理、缓存加载器、缓存监听器、缓存信息统计...等功能。
| IO模块               | 一系列处理IO（输入/输出）操作的工具，包括 FileTool、CsvTool、IOTool...等。
| Concurrent模块       | 一系列并发编程工具，具备良好的线程安全、高并发及高性能优势，包括MessageQueue（高性能内存队列，30W+ TPS）、CyclicThread（后台循环线程）、TimeWheel（时间轮组件）、TokenBucket（令牌桶/限流器）等。
| Http模块             | 一系列处理Http通讯、IP、Cookie等相关工具。
| Json模块             | json序列化、反序列化工具封装，基于Gson。
| JsonRpc模块          | 一个轻量级、跨语言远程过程调用实现，基于json、http实现（对比传统RPC框架：[XXL-RPC](https://github.com/xuxueli/xxl-rpc)）。
| Excel模块            | 一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel之间的转换。
| Emoji模块            | 一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码。
| Response模块         | 统一响应数据结构体，标准化数据结构、状态码等，降低协作成本。
| Pipeline模块         | 高扩展性流程编排引擎。
| Error模块            | 异常处理相关工具，包括通用业务异常封装、异常工具类等；
| Freemarker模块       | 模板引擎工具，支持根据模板文件实现 动态文本生成、静态文件生成 等，支持邮件发送、网页静态化场景。
| Crypto模块           | 一系列处理编解码、加解密的工具，包括 Md5Tool、Sha256Tool、HexTool、Base64Tool...等。
| Auth模块             | 一系列权限认证相关工具，包括JwtTool...等。
| ID模块               | 一系列ID生成工具，支持多种ID生成策略，包括 UUID、Snowflake、Date、Random 等。
| Serializer模块       | 一系列序列化、反序列化工具，支持扩展多种序列化格式，包括 jdk、protobuf、hessian 等。
| Captcha模块          | 一个验证码工具，支持随机字符验证码、数字验证码、中文验证码等多形式。支持自定义验证码生成算法、宽高、颜色、文字字体/大小/间距、背景颜色、边框宽度/边框、干扰策略…等。
| DataStructure模块    | 一系列数据结构工具，包括 BloomFilter、Trie/前缀树...等； 
| ...                | ...

### 1.4 下载

#### 文档地址

- [中文文档](https://www.xuxueli.com/xxl-tool/)

#### 源码仓库地址

源码仓库地址 | Release Download
--- | ---
[https://github.com/xuxueli/xxl-tool](https://github.com/xuxueli/xxl-tool) | [Download](https://github.com/xuxueli/xxl-tool/releases)
[https://gitee.com/xuxueli0323/xxl-tool](https://gitee.com/xuxueli0323/xxl-tool) | [Download](https://gitee.com/xuxueli0323/xxl-tool/releases)  


#### 技术交流
- [社区交流](https://www.xuxueli.com/page/community.html)

### 1.5 环境
- Maven：3+
- Jdk：17+ (说明：版本2.x开始要求Jdk17；版本1.x及以下支持Jdk1.8。如对Jdk版本有诉求，可选择接入不同版本。)

### 1.6 Maven依赖
```
<!-- http://repo1.maven.org/maven2/com/xuxueli/xxl-tool/ -->
<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-tool</artifactId>
    <version>${最新稳定版}</version>
</dependency>
```

### 1.7 发展历程
XXL-TOOL 前身为  XXL-EXCEL、XXL-EMOJI 两个独立项目，以及 XXL-JOB 内部经过验证的成熟工具类等，经过整合演进最终诞生。
- 1、XXL-EXCEL：首版发布于2017年9月，一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel文档之间的转换。同时保证性能和稳定。（已废弃，整合至 XXL-TOOL）
- 2、XXL-EMOJI：首版发布于2018年7月，一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码。（已废弃，整合至 XXL-TOOL）


## 二、接入指南

### Tool明细
| 模块              | 工具               |  说明                                  
|-----------------|------------------|-------------------------------------|
| core            | StringTool       | 字符串工具，提供字符串校验及操作相关能力
| core            | DateTool         | 日期时间工具，提供日期时间转换及操作相关能力
| core            | AssertTool       | 断言工具，提供有效性校验能力
| core            | CollectionTool   | 集合工具，提供集合操作能力
| core            | ArrayTool        | 数组工具，提供集合操作能力
| core            | MapTool          | Map 工具，提供Map操作能力
| core            | ObjectTool       | Object工具，提供Object操作能力
| core            | PropTool         | Prop工具，提供Properties文件操作能力
| core            | ReflectionTool   | Java反射工具，提供Java反射操作能力
| core            | ClassTool        | Class类工具，提供Class类操作能力
| core            | TypeTool         | Type工具，提供Type操作能力
| core            | RegexTool        | 正则工具，提供正则校验及操作能力
| auth            | JwtTool          | JWT工具，提供JWT生成及解析能力
| cache           | CacheTool        | 一个高性能的 Java 缓存工具，支持多种缓存类型（FIFO、LFU、LRU等）、锁分桶优化、缓存过期策略（写后过期、访问后过期...）、缓存定时清理、缓存加载器、缓存监听器、缓存信息统计...等功能。
| concurrent      | CyclicThread     | 后台循环线程，支持精准、线程安全的周期性循环执行能力
| concurrent      | MessageQueue     | 高性能内存队列，单机支持 30W+ TPS
| concurrent      | TimeWheel        | 时间轮组件，提供定时任务执行能力
| concurrent      | TokenBucket      | 令牌桶/限流器组件，提供令牌桶限流能力
| emoji           | EmojiTool        | Emoji表情工具，提供Emoji表情编解码能力
| crypto          | Base64Tool       | Base64工具，提供Base64编解码能力
| crypto          | HexTool          | Hex工具，提供Hex编解码能力
| crypto          | Md5Tool          | MD5工具，提供MD5编码能力
| crypto          | SHA256Tool       | SHA256工具，提供SHA256编码能力
| excel           | ExcelTool        | 一个基于注解的 Excel 与 Java对象 相互转换及导入导出工具；一行代码完成Java对象和Excel之间的转换。
| exception       | BizException     | 通用业务异常
| exception       | ThrowableTool    | 异常处理工具
| freemarker      | FtlTool          | 模板引擎工具, 支持根据模板文件实现 动态文本生成、静态文件生成 等，支持邮件发送、网页静态化场景。
| json            | GsonTool         | Json序列化及反序列化工具，基于Gson
| http            | CookieTool       | Cookie工具，提供Cookie读写操作能力
| http            | HttpTool         | 一个高性能 HTTP 请求库，API简洁易用、使用高效方便且性能优越；支持 “常规Http请求、Java对象请求、接口&注解编程” 三种使用方式。
| http            | IPTool           | IP工具，提供IP地址及端口号相关校验、生成及操作相关能力
| io              | IOTool           | IO工具，提供丰富IO读写操作能力
| io              | FileTool         | 一个高性能 File/文件 操作工具，支持丰富文件操作API；针对大文件读写设计分批操作、流式读写能力，降低内存占用、提升文件操作性能。
| io              | CsvTool          | Csv工具，提供Csv文件读写操作能力
| jsonrpc         | JsonRpcClient    | 轻量级RPC通讯工具，客户端实现；基于json、http实现
| jsonrpc         | JsonRpcServer    | 轻量级RPC通讯工具，服务端实现；基于json、http实现
| pipeline        | PipelineExecutor | Pipeline执行工具，提供pipeline注册管理以及执行相关能力
| pipeline        | Pipeline         | Pipeline工具，提供pipeline定义及执行相关能力
| response        | Response         | 标准响应结果封装，统一服务端数据返回格式
| response        | ResponseCode     | 标准响应码定义，统一服务端响应码体系
| response        | PageModel        | 标准分页结果封装，统一服务端分页数据格式
| id              | DateIdTool       | ID生成工具，根据日期趋势递增生成ID；
| id              | RandomIdTool     | ID生成工具，随机数字、字母、混合字符生成工具；
| id              | SnowflakeIdTool  | ID生成工具，雪花算法ID生成工具；
| id              | UUIDTool         | ID生成工具，UUID生成工具；
| captcha         | CaptchaTool      | 验证码工具，提供验证码生成能力；
| datastructure   | BloomFilter      | 布隆过滤器，一种基于多哈希函数和位数组的概率型数据结构，具有高效空间利用与快速查询特性；
| datastructure   | Trie             | 前缀数，一种哈希树的变种，利用公共前缀来节省存储空间和提高查询效率；
| ...             | ...              


### 2.1、Core模块

参考单元测试，见目录：com.xxl.tool.test.core
```
// DateTool
DateTool.formatDateTime(date);                  // 时间格式化，格式为："yyyy-MM-dd HH:mm:ss"
DateTool.formatDate(date);                      // 时间格式化，格式为："yyyy-MM-dd"
DateTool.format(date, "yyyy-MM-dd HH");         // 时间格式化，自定义格式
DateTool.parseDateTime(dateTimeStr);            // 时间解析，解析字符串格式为："yyyy-MM-dd HH:mm:ss"
DateTool.formatDate(dateTimeStr);               // 时间解析，解析字符串格式为："yyyy-MM-dd"
DateTool.addYears(date, 1);                     // 新增1年；
DateTool.addMonths(date, 1);                    // 新增1月
DateTool.addDays(date, 1);                      // 新增1天
DateTool.addHours(date, 1);                     // 新增1小时
DateTool.addMinutes(date, 1);                   // 新增1分钟
DateTool.addSeconds(date, 1);                   // 新增1秒
DateTool.addWeeks(date, 1);                     // 新增1周
DateTool.addMilliseconds(date, 1);              // 新增1毫秒
DateTool.betweenYear(a, b);                     // 计算两个时间间隔的年数
DateTool.betweenMonth(a, b);                    // 计算两个时间间隔的月数
DateTool.betweenDay(a, b);                      // 计算两个时间间隔的天数
DateTool.betweenHour(a, b);                     // 计算两个时间间隔的小时数
DateTool.betweenMinute(a, b);                   // 获取两个时间间隔的分钟数
DateTool.betweenSecond(a, b);                   // 获取两个时间间隔的秒数
DateTool.set(date, calendarField, amount);      // 设置时间的单个属性值，参数：时间、属性（如年、月、日等）、数量（正数表示增加，负数表示减少）
DateTool.setYears(date, b);                     // 设置时间的年属性值
DateTool.setDay(date, b);                       // 获取时间的天属性值
...
DateTool.isSameDay(a, b);                       // 判断两个时间是否是同一天

// CollectionTool
CollectionTool.isEmpty(list);           // 判断是否为空
CollectionTool.isEmpty(contains, a);    // 判断是否包含某元素
CollectionTool.union(a,b);              // 并集
CollectionTool.intersection(a,b);       // 交集
CollectionTool.disjunction(a,b);        // 交集的补集
CollectionTool.subtract(a,b);           // 集合a减去集合b
CollectionTool.subtract(b,a);           // 集合b减去集合a
CollectionTool.newArrayList();
CollectionTool.newArrayList(1,2,3);     // 快速创建集合
CollectionTool.split(dataList, 50)      // 快速切割集合，每50条拆分一个集合

// StringTool
StringTool.isEmpty("  ");
StringTool.isBlank("  ");

// MapTool
MapTool.isNotEmpty(map);
MapTool.getInteger(map, "k1");    // 根据类型（Integer），获取数据
MapTool.getLong(map, "k1");       // 根据类型（Long），获取数据 
MapTool.newHashMap(               // 快速创建map，支持 key-value 键值对初始化
        "k1", 1,
        "k2", 2
))

// BeanTool
UserDTO userDTO = BeanTool.copyProperties(user, UserDTO.class);   // 对象属性拷贝

// ReflectionTool
ReflectionTool.getMethod(object, "method");                      // 获取对象Method
ReflectionTool.getField(object, "name");                         // 获取对象Field
ReflectionTool.setFieldValue(object, field, "123");              // 设置对象Field值
ReflectionTool.doWithFields(DemoDTO.class, field -> {            // 遍历对象Field
    logger.info("doWith - field = {}", field);
});

// ReflectionTool
RegexTool.matches("1[3-9]\\d{9}", "12345678901");               // 验证字符串是否匹配正则表达式          
RegexTool.findAll("[a-z]+", "ABC123def456ghi789");              // 查找所有匹配正则表达式的子串
RegexTool.extract("(\\d+)-(\\d+)", "phone: 123-456", 0);        // 提取匹配的内容
RegexTool.replaceAll("xyz", "hello world", "test");             // 替换所有匹配正则表达式的子串
RegexTool.split(",", "apple,banana,orange");                    // 按照正则表达式分割字符串

// …… 更多请查阅API
```

### 2.2、Json模块

**代码示例**：参考单元测试：com.xxl.tool.test.response.GsonToolTest
```
// Object 转成 json
String json = GsonTool.toJson(new Demo());

// json 转成 特定的cls的Object
Demo demo = GsonTool.fromJson(json, Demo.class);
    
// json 转成 特定的 rawClass<classOfT> 的Object
Response<Demo> response = GsonTool.fromJson(json, Response.class, Demo.class);

// json 转成 特定的cls的 ArrayList
List<Demo> demoList = GsonTool.fromJsonList(json, Demo.class);

// json 转成 特定的cls的 HashMap
HashMap<String, Demo> map = GsonTool.fromJsonMap(json, String.class, Demo.class);

// …… 更多请查阅API
```

**依赖说明**：该模块需要主动引入如下关联依赖（默认provided模式，精简不必须依赖），可参考仓库pom获取依赖及版本：https://github.com/xuxueli/xxl-tool/blob/master/pom.xml
```
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>${gson.version}</version>
</dependency>
```

### 2.3、Cache模块

一个高性能的 Java 缓存工具，支持多种缓存类型（FIFO、LFU、LRU等）、锁分桶优化、缓存过期策略（写后过期、访问后过期...）、缓存定时清理、缓存加载器、缓存监听器、缓存信息统计...等功能。

参考单元测试：com.xxl.tool.test.cache.CacheToolTest

```
// 1、快速创建缓存
Cache<String, String> cache = CacheTool.newFIFOCache(1000).build();   // 默认FIFO缓存
Cache<String, String> cache = CacheTool.newLFUCache(1000).build();    // LFU缓存
Cache<String, String> cache = CacheTool.newLRUCache(1000).build();    // LRU缓存
Cache<String, String> cache = CacheTool.newUnlimitedCache().build();  // 无限制缓存

// 2、缓存详细配置
Cache<String, String> cache2 = CacheTool.newLRUCache()
                .capacity(1000)                 // 缓存容量
                .expireAfterAccess(30 * 1000)   // 缓存过期时间 30s，过期策略为：访问后过期
                .expireAfterWrite(30 * 1000)    // 缓存过期时间 30s，过期策略为：写后过期  （expireAfterAccess 与 expireAfterWrite，选择其一设置即可）
                .pruneInterval(5000)            // 定期清理缓存，清理间隔为5s
                .build();
                
// 3、缓存加载器设置
Cache<String, String> cache = CacheTool.<String,String>newLRUCache()
                .loader(new CacheLoader<>() {               // 自定义缓存加载器，缓存未命中时，会调用该加载器获取数据
                    @Override
                    public String load(String key) throws Exception {
                        return "value-" + key;
                    }
                })
                .build();
                
// 4、缓存监听器设置
Cache<String, String> cache = CacheTool.<String,String>newLRUCache()
                .listener(new CacheListener<>() {             // 缓存监听器，缓存删除操作时，会调用该监听器
                    @Override
                    public void onRemove(String key, String value) throws Exception {
                        logger.info("onRemove, key = " + key + ", value = " + value);
                    }
                })
                .build();
                

// 5、缓存常规操作；
cache.put(key, "value01");    // 写入缓存
cache.get(key);               // 获取缓存，缓存未命中时，尝试从缓存加载器加载数据（若已设置缓存加载器）
cache.getIfPresent(key);      // 获取缓存，缓存未命中时返回null
cache.remove(key);            // 删除缓存

// 6、缓存其他操作
cache.prune()                 // 清理已过期缓存
cache.clear();                // 清空缓存
cache.asMap()                 // 获取全部缓存数据（过滤已过期数据）
cache.size()                  // 缓存对象数量（包含过期数据）
cache.hitCount()              // 缓存命中次数
cache.missCount()             // 缓存未命中次数
cache.isEmpty()               // 缓存是否为空
cache.isFull()                // 缓存是否已满
...

```

### 2.4、Response模块

参考单元测试：com.xxl.tool.test.response.ResponseBuilderTest
```
// 构建结果
Response.ofSuccess();                          // 快速构建成功响应
Response.ofSuccess("正文数据");                 // 快速构建成功响应，并设置正文数据
Response.ofFail();                             // 快速构建失败响应
Response.ofFail("提示消息");                    // 快速构建失败响应，并设置提示消息
Response.of(200, "提示消息", {正文数据});        // 完整构建响应：响应码、提示消息、正文数据
Response.of(200, "提示消息");                   // 完成构建响应：响应码、提示消息

// 获取结果数据
result.getCode()                                // 获取响应码
result.getMsg()                                 // 获取提示消息
result.getData()                                // 获取正文数据
result3.isSuccess()                             // 响应是否成功，状态码为200时为成功，其他为失败

// 预设响应码
ResponseCode.SUCCESS
ResponseCode.FAILURE
...
```

### 2.5、Pipeline模块

**案例1：执行单个pipeline**        
说明：开发业务逻辑节点handler，定义编排单个pipeline；模拟执行参数，运行pipeline，获取响应结果。

参考单元测试：com.xxl.tool.test.pipeline.PipelineTest
```
// 开发业务逻辑节点handler
PipelineHandler handler1 = new Handler1();
PipelineHandler handler2 = new Handler2();
PipelineHandler handler3 = new Handler3();

// 定义编排单个pipeline
Pipeline p1 = new Pipeline()
        .name("p1")
        .status(PipelineStatus.RUNTIME.getStatus())
        .addLasts(handler1, handler2, handler3);

// 模拟执行参数
DemoRequest requet = new DemoRequest("abc", 100);

// 执行 pipeline
Response<Object>  response = p1.process(requet);
```

**案例2：执行单个pipeline**        
说明：开发业务逻辑节点handler，定义编排多个pipeline；定义pipeline执行器，并注册多个pipeline； 模拟执行参数，通过 pipeline 执行器路由 并 执行 pipeline，获取响应结果。

参考单元测试：com.xxl.tool.test.pipeline.PipelineExecutorTest
```
// 开发业务逻辑节点handler
PipelineHandler handler1 = new Handler1();
PipelineHandler handler2 = new Handler2();
PipelineHandler handler3 = new Handler3();

// 定义编排多个pipeline
Pipeline p1 = new Pipeline()
        .name("p1")
        .status(PipelineStatus.RUNTIME.getStatus())
        .addLasts(handler1, handler2, handler3);

Pipeline p2 = new Pipeline()
        .name("p2")
        .status(PipelineStatus.RUNTIME.getStatus())
        .addLasts(handler2, handler1, handler3);

// 定义pipeline执行器，并注册多个pipeline
PipelineExecutor executor = new PipelineExecutor();
executor.registry(p1);
executor.registry(p2);

// 模拟执行参数
PipelineTest.DemoRequest requet1 = new PipelineTest.DemoRequest("aaa", 100);
PipelineTest.DemoRequest requet2 = new PipelineTest.DemoRequest("bbb", 100);

// 通过 pipeline 执行器路由 并 执行 pipeline
Response<Object> response1 = p1.process(requet1);
logger.info("response1: {}", response1);
Assertions.assertEquals(response1.getCode(), ResponseCode.CODE_200.getCode());

Response<Object>  response2 = p2.process(requet2);
logger.info("response2: {}", response2);
Assertions.assertEquals(response2.getCode(), ResponseCode.CODE_200.getCode());
```

### 2.6、Excel模块

**功能定位**

一个基于注解的 Excel 与 Java对象 相互转换及导入导出工具；一行代码完成Java对象和Excel之间的转换。
（原名 XXL-EXCEL，整合至该项目）

**特性**
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；
- 3、全基础数据类型支持：Excel的映射Java对象支持设置任意基础数据类型，将会自动完整值注入；
- 4、多Sheet导出：导出Excel时支持设置多张sheet；
- 5、多Sheet导入：导入Excel时支持设置多张sheet，通过 "@ExcelSheet.name" 注解匹配Sheet;

**Java 对象 和 Excel映射关系**

|----------- | Excel                 | Java 对象                    |
|------------|-----------------------|----------------------------|
| 表          | Sheet                 | Java对象列表                   |
| 表头         | Sheet首行               | Java对象Field                |
| 数据         | Sheet一行记录             | Java对象列表中一个元素              |

**核心注解：ExcelSheet**

功能：描述Sheet信息，注解添加在待转换为Excel的Java对象类上，可选属性如下。

| ExcelSheet       | 说明                    |
|------------------|-----------------------|
| name             | 表/Sheet名称             |     
| headColor        | 表头/Sheet首行的颜色         |    

**核心注解：ExcelField**

功能：描述Sheet的列信息，注解添加在待转换为Excel的Java对象类的字段上，可选属性如下。

| ExcelField      | 说明                                                |
|-----------------|---------------------------------------------------|
| name            | 属性名/列名称                                           |
| width           | 列宽                                                |
| align           | 对齐方式，LEFT、RIGHT、CENTER...                         |
| dateformat      | 针对 Date 类型数据，日期格式化形式，默认 "yyyy-MM-dd HH:mm:ss"     |
| ignore          | 该字段是否忽略，默认false                                   |


**使用指南**
- a、引入依赖

该模块需要主动引入如下关联依赖（默认provided模式，精简不必须依赖），可参考仓库pom获取依赖及版本：https://github.com/xuxueli/xxl-tool/blob/master/pom.xml
```
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>${poi.version}</version>
</dependency>
```

- b、定义Java对象    

```java
@ExcelSheet(name = "商户列表", headColor = IndexedColors.LIGHT_GREEN)
public class ShopDTO {

    @ExcelField(name = "商户ID")
    private int shopId;

    @ExcelField(name = "商户名称")
    private String shopName;

    public ShopDTO() {
    }
    public ShopDTO(int shopId, String shopName) {
        this.shopId = shopId;
        this.shopName = shopName;
    }

    // set、get
}
```

- c、Excel导入、导出代码

常规方式：   
```
// 参考测试代码：com.xxl.tool.test.excel.ExcelToolTest

/**
 * Excel导出：Object 转换为 Excel
 */
ExcelTool.writeFile(filePath, shopDTOList);

/**
 * Excel导入：Excel 转换为 Object
 */
List<ShopDTO> shopDTOList = ExcelTool.readExcel(filePath, ShopDTO.class);
```

流式方式（支持大数据量）：
```
/**
 * Excel导出（流式方式）：Object 转换为 Excel 
 */
ExcelTool.writeExcel(filePath, new Supplier<>() {
    @Override
    public UserDTO get() {
        // 流式获取数据 ...
        return new UserDTO();
    }
});

/**
 * Excel导入（流式方式）：Excel 转换为 Object
 */
ExcelTool.readExcel(filePath, new Consumer<UserDTO>() {
    @Override
    public void accept(UserDTO userDTO) {
        logger.info("item: " + userDTO);
    }
});
```


### 2.7、Emoji模块

**功能定位**
一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码.
（原名 XXL-EMOJI）

**特性**
- 1、简洁：API直观简洁，一分钟上手；
- 2、易扩展：模块化的结构，可轻松扩展；
- 3、别名自定义：支持为Emoji自定义别名；
- 4、实时性：实时收录最新发布的Emoji；

**Emoji编码类型**

概念 | 说明
--- | ---
EmojiEncode.ALIASES | 将Emoji表情转换为别名，格式为 ": alias :"；
EmojiEncode.HTML_DECIMAL | 将Emoji表情Unicode数据转换为十进制数据；
EmojiEncode.HTML_HEX_DECIMAL | 将Emoji表情Unicode数据转换为十六进制数据；

**Emoji编解码API**

API | 说明
--- | ---
public static String encodeUnicode(String input, EmojiTransformer transformer, FitzpatrickAction fitzpatrickAction) | Emoji表情编码方法，支持自定义编码逻辑；
public static String encodeUnicode(String input, EmojiEncode emojiEncode, FitzpatrickAction fitzpatrickAction) | Emoji表情编码方法，支持自定义编码类型；
public static String encodeUnicode(String input, EmojiEncode emojiEncode) | Emoji表情编码方法，支持自定义编码类型；
public static String encodeUnicode(String input) | Emoji表情编码方法，编码类型默认为 "ALIASES" ；
public static String decodeToUnicode(String input) | Emoji表情解码方法，支持针对 "ALIASES、HTML_DECIMAL、HTML_HEX_DECIMAL" 等编码方式解码；
public static String removeEmojis(String input, final Collection<Emoji> emojisToRemove, final Collection<Emoji> emojisToKeep) | 清除输入字符串中的Emoji数据；
public static List<String> findEmojis(String input) | 查找输入字符转中的全部Emoji数据列表；

**自定义Emoji别名**

略

**使用指南**

- a、使用示例  

```java
// 参考测试代码：com.xxl.tool.test.emoji.EmojiToolTest

String input = "一朵美丽的茉莉🌹";
System.out.println("unicode：" + input);

// 1、alias
String aliases = EmojiTool.encodeUnicode(input, EmojiEncode.ALIASES);
System.out.println("\naliases encode: " + aliases);
System.out.println("aliases decode: " + EmojiTool.decodeToUnicode(aliases, EmojiEncode.ALIASES));

// 2、html decimal
String decimal = EmojiTool.encodeUnicode(input, EmojiEncode.HTML_DECIMAL);
System.out.println("\ndecimal encode: " + decimal);
System.out.println("decimal decode: " + EmojiTool.decodeToUnicode(decimal, EmojiEncode.HTML_DECIMAL));

// 3、html hex decimal
String hexdecimal = EmojiTool.encodeUnicode(input, EmojiEncode.HTML_HEX_DECIMAL);
System.out.println("\nhexdecimal encode: " + hexdecimal);
System.out.println("hexdecimal decode: " + EmojiTool.decodeToUnicode(hexdecimal, EmojiEncode.HTML_HEX_DECIMAL));
        
```

- b、运行日志输出    

```text
aliases encode: 一朵美丽的茉莉:rose:
aliases decode: 一朵美丽的茉莉🌹

decimal encode: 一朵美丽的茉莉&#127801;
decimal decode: 一朵美丽的茉莉🌹

hexdecimal encode: 一朵美丽的茉莉&#x1f339;
hexdecimal decode: 一朵美丽的茉莉🌹
```

### 2.8、Freemarker 模块

**代码示例**：参考单元测试，见目录：com.xxl.tool.test.freemarker.FtlTool
```
// 初始化设置 模板文件目录地址
FtlTool.init("/Users/admin/Downloads/");

// 根据模板文件，生成文本；支持传入变量
String text = FtlTool.processString("test.ftl", new HashMap<>());
logger.info(text);
```

**依赖说明**：该模块需要主动引入如下关联依赖（默认provided模式，精简不必须依赖），可参考仓库pom获取依赖及版本：https://github.com/xuxueli/xxl-tool/blob/master/pom.xml
```
<dependency>
    <groupId>org.freemarker</groupId>
    <artifactId>freemarker</artifactId>
    <version>${freemarker.version}</version>
</dependency>
```

### 2.9、Http 模块

一个高性能 HTTP 请求库，API简洁易用、使用高效方便且性能优越；支持 “常规Http请求、Java对象请求、接口&注解编程” 三种使用方式。

参考单元测试，见目录：com.xxl.tool.test.http.HttpToolTest

- **a、常规Http请求**：

```
// 1、发送 Get 请求，获取响应内容
String response = HttpTool.createPost("https://news.baidu.com/widget?ajax=json&id=ad").execute().response();

// 2、发送 Post 请求，获取 Http状态码 以及 响应内容
HttpResponse httpResponse = HttpTool.createPost("https://news.baidu.com/widget?ajax=json&id=ad").execute();
int statusCode = httpResponse.statusCode();   // 获取Http状态码
String response = httpResponse.response();    // 获取响应内容

// 3、自定义请求参数
HttpResponse httpResponse = HttpTool.createRequest()
                .url("https://news.baidu.com/widget?ajax=json&id=ad")     // 设置请求地址
                .method(Method.GET)                                       // 设置请求方式
                .contentType(ContentType.JSON)                            // 设置请求内容类型
                .header("header", "value")                                // 设置请求头/header
                .cookie("cookie", "value")                                // 设置Cookie
                .connectTimeout(10000)                                    // 设置连接超时时间
                .readTimeout(10000)                                       // 读取超时
                .useCaches(false)                                         // 设置是否使用缓存
                .body("body")                                             // 设置请求体， 仅针对 非Get 请求生效
                .form("form", "value")                                    // 设置表单参数，仅针对 GET 请求生效，参数将会添加到 url 中；
                .auth("auth999")                                          // 设置认证信息，本质为设置 header（Authorization） 信息； 
                .interceptor(new HttpInterceptor() {                      // 添加拦截器
                    @Override
                    public void before(HttpRequest httpRequest) {
                        logger.info("before, url = " + httpRequest.getUrl());
                    }
                    @Override
                    public void after(HttpRequest httpRequest, HttpResponse httpResponse) {
                        logger.info("after, response = " + httpResponse.response());
                    }
                });
int statusCode = httpResponse.statusCode();   // 获取Http状态码
String response = httpResponse.response();    // 获取响应内容

// 4、获取服务端返回的 Cookie 信息
HttpResponse httpResponse = HttpTool.createGet("https://news.baidu.com/widget?ajax=json&id=ad").execute();
String cookie = httpResponse.cookie("key");   // 获取服务端返回的 Cookie 信息


```

- **b、Java对象请求**：

基于Java对象形式交互，提效开发效率：提交Request对象、获取服务端返回的Response对象，工具底层自动处理json序列化/反序列化工作；

```
RespDTO result = HttpTool.createPost("https://news.baidu.com/widget?ajax=json&id=ad")
                .request(new RespDTO("jack", 18))   // 设置请求java对象数据，将会自动序列化为json，以 requestBody 形式发送；
                .execute()
                .response(RespDTO.class);           // 设置响应java对象类型，将会自动将响应内容 反序列化 为java对象；
```

- **c、接口&注解编程**：
  
基于接口&注解编程，极致提效Http请求：定义请求接口，支持注解式设置请求参数，工具底层自动处理请求地址生成、参数设置，以及出入参序列化/反序列化等工作；

使用示例01：
```
// 1、接口定义（普通接口）
public static interface DemoService2{
    RespDTO widget();
}

// 2、接口代理，发送请求
DemoService demoService = HttpTool.createClient()
                .url("https://news.baidu.com/widget?ajax=json&id=ad")
                .timeout(10000)
                .proxy(DemoService.class);
RespDTO result = demoService.widget();


```

使用示例02：
```
// 1、接口定义（注解 + 接口）
@HttpClientService(url = "https://news.baidu.com/widget?ajax=json&id=ad")
public static interface DemoService2{
    @HttpClientMethod(timeout = 10000)
    RespDTO widget();
}

// 2、接口代理，发送请求
DemoService2 demoService = HttpTool.createClient().proxy(DemoService2.class);
RespDTO result = demoService.widget();
```

### 2.10、IP 模块

参考单元测试，见目录：com.xxl.tool.test.http.IPToolTest
```
// Port相关
IPTool.isPortInUsed(port);    
IPTool.isValidPort(port);
IPTool.getRandomPort();
IPTool.getAvailablePort();

// Host相关
IPTool.isLocalHost(host));
IPTool.isAnyHost(host));
IPTool.isValidLocalHost(host));
IPTool.getIp();   // 兼容多网卡

// Address相关
IPTool.isValidV4Address(address)
IPTool.toAddressString(new InetSocketAddress(host, port)));
IPTool.toAddress(address));
```

### 2.11、JsonRpc  

**功能定位**
一个轻量级、跨语言远程过程调用实现，基于json、http实现（传统RPC框架对比：[XXL-RPC](https://github.com/xuxueli/xxl-rpc)）。

**代码示例：**     
参考单元测试，见目录：
- com.xxl.tool.test.jsonrpc.service.UserService：RPC业务代码
- com.xxl.tool.test.jsonrpc.TestServer：服务端代码
- com.xxl.tool.test.jsonrpc.TestClient：客户端代码

RPC业务服务开发：
```
public interface UserService {
    public ResultDTO createUser(UserDTO userDTO);
    public UserDTO loadUser(String name);
    ... ...
}
```

JsonRpc服务端配置：
```
// a、JsonRpcServer 初始化
JsonRpcServer jsonRpcServer = new JsonRpcServer();

// b、业务服务注册（支持多服务注册）
jsonRpcServer.register("userService", new UserServiceImpl());

// c、Web框架集成，该入口为RPC统一流量入口（示例A：springmvc 集成；理论上支持任意web框架集成，其他框架参考集成）
@RequestMapping("/openapi")
@ResponseBody
public String api(@RequestBody(required = false) String requestBody){
    // 核心代码：入参 RequestBody 作为入参，返回字符串作为响应结果；
    return jsonRpcServer.invoke(requestBody);
}

// c、Web框架集成，该入口为RPC统一流量入口（示例B：原生 HttpServer 集成；）
HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
httpServer.createContext("/openapi", new HttpHandler() {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        ... ...
        // 核心代码：入参 RequestBody 作为入参，返回字符串作为响应结果；
        String jsonRpcResponse = jsonRpcServer.invoke(requestBody);
        ... ...        
    }
});
```

JsonRpc客户端配置：
```
// 方式1：代理方式使用 （针对接口构建代理，通过代理对象实现远程调用；）
UserService userService = new JsonRpcClient("http://localhost:8080/jsonrpc", 3000).proxy("userService", UserService.class);
UserDTO result = userService.loadUser("zhangsan");


// 方式2：常规客户端方式 （针对目标地址构建Client，手动设置请求细节参数；）
JsonRpcClient jsonRpcClient = new JsonRpcClient("http://localhost:8080/jsonrpc", 3000);
UserDTO result2 = jsonRpcClient.invoke(
        "userService",                    // 服务名称
        "loadUser",                       // 方法名称
        new Object[]{ "zhangsan" },       // 参数列表
        UserDTO.class);                   // 返回类型
```

**依赖说明**：该模块需要主动引入如下关联依赖（默认provided模式，精简不必须依赖），可参考仓库pom获取依赖及版本：https://github.com/xuxueli/xxl-tool/blob/master/pom.xml
```
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>${gson.version}</version>
</dependency>
```

### 2.12、Concurrent模块

一系列并发编程工具，具备良好的线程安全、高并发及高性能优势，包括后台循环线程（CyclicThread）、高性能内存队列（MessageQueue）等。

**MessageQueue （高性能内存队列）**            
说明：高性能内存队列，单机支持 30W+ TPS，具备良好的性能及高并发优势，支持生产消费模型。

参考单元测试，见目录：com.xxl.tool.test.concurrent.MessageQueueTest
```
// a、定义队列：指定 消费者数量、批量消费数量、消费者逻辑等
MessageQueue<Long> messageQueue = new MessageQueue<>(
        "demoQueue",                // 队列名称
        10000,                      // 队列容量
        3,                          // 队列消费线程数
        new Consumer<Long>() {      // 消费者消费逻辑
            @Override
            public void accept(Long data) {
                System.out.println("消费: -" + data + ");
            }
        }
);

// b、生产消息
messageQueue.produce(addData);

// c、停止队列（可选）
messageQueue.stop();
```

**CyclicThread （后台/循环线程）**    
说明：专注于周期性执行/后台服务场景，具备良好的线程安全和异常处理机制。

参考单元测试，见目录：com.xxl.tool.test.concurrent.CyclicThread
```
// 定义循环线程
CyclicThread threadHelper = new CyclicThread(
      "demoCyclicThread",     // 线程名称
      true,                   // 是否后台执行
      200,                    // 循环执行时间间隔（单位：毫秒）
      new Runnable() {        // 线程执行逻辑
          @Override
          public void run() {
              System.out.println("thread running ... ");
          }
      });
                
// 启动
threadHelper.start();

// 停止
threadHelper.stop();
```

**TimeWheel （时间轮）**
说明：时间轮算法实现，具备高精度、多任务、以及线程安全等优势。
参考单元测试，见目录：com.xxl.tool.test.concurrent.TimeWheelTest
```
// a、时间轮定义，自定义时间轮刻度、间隔等
TimeWheel timeWheel = new TimeWheel(60, 1000);
timeWheel.start();

// b、提交时间轮任务（定时任务）
timeWheel.submitTask(System.currentTimeMillis() + 3000, () -> {
    System.out.println("Task delay " + 3000 + "ms running.");
});
```

**TokenBucket （令牌桶/限流器）**

说明：令牌桶算法实现，具备高精度、预热支持以及线程安全等优势。
参考单元测试，见目录：com.xxl.tool.test.concurrent.TokenBucketTest
```
// a、令牌桶定义，自定义设置每秒获取的令牌数
TokenBucket smoothBursty = TokenBucket.create(5.0);

// b、获取令牌, 返回获取的令牌耗时
double cost = smoothBursty.acquire();

// c、获取令牌, 尝试获取令牌, 100毫秒内返回结果；获取不到时返回false
boolean result = smoothBursty.tryAcquire(100, TimeUnit.MILLISECONDS);
```

### 2.13、Auth模块   
一系列权限认证相关工具

**Jwt认证：**    
JWT工具，提供JWT生成及解析能力

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.auth.JwtToolTest
```
// JwtTool 初始化
String SECRET = "your-256-bit-secret-key-should-be-at-least-32-bytes";
JwtTool jwtTool = new JwtTool(SECRET);    // 默认使用 MACSigner/MACVerifier，支持多构造方法自定义实现；
        
// 生成token
String token = jwtTool.createToken(
                {用户标识},
                {自定义声明数据，map形式},
                {自定义过期时间}
        );
        
// 验证token
boolean isValid = jwtTool.validateToken(token);   

// 获取claim
Object userId = jwtTool.getClaim(token, {自定义声明数据key});

// 获取过期时间
Date expirationTime = jwtTool.getExpirationTime(token);
```

**依赖说明**：该模块需要主动引入如下关联依赖（默认provided模式，精简不必须依赖），可参考仓库pom获取依赖及版本：https://github.com/xuxueli/xxl-tool/blob/master/pom.xml
```
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>${nimbus-jose-jwt.version}</version>
</dependency>
```

### 2.14、Serializer模块

一系列序列化、反序列化工具，支持扩展多种序列化格式，包括 jdk、protobuf、hessian 等。

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.serializer.SerializerTest
```
// a、匹配序列化工具
Serializer serializer = SerializerEnum.JAVA.getSerializer();

// b、序列化 Java 对象 （ DemoUser 为示例对象）
DemoUser demoUser = new DemoUser("jack", 18);
byte[] bytes = serializer.serialize(demoUser);

// b、反序列化 Java 对象
DemoUser demoUser2 = serializer.deserialize(bytes);
logger.info("demoUser2: {}", demoUser2);
```

### 2.15、ID模块

一系列ID生成工具，支持多种ID生成策略，包括 UUID、Snowflake、Date、Random 等。

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.id
```
// a、日期方式ID生成
DateIdTool.getDateId();                     // 输出格式：20250713115530671505

// b、雪花算法方式ID生成
SnowflakeIdTool idGen = new SnowflakeIdTool(1);
idGen.nextId());                          // 输出格式：7350010799378665472

// c、UUID方式ID生成
UUIDTool.getUUID();                       // 输出格式：21765f7c-8c47-4418-9a72-a3e5c88be06c
UUIDTool.getSimpleUUID();                 // 输出格式：cf665741604b4f309cd59d142ee007e3

// d、随机方式ID生成
RandomIdTool.getDigitId(10);                // 输出格式：63484898497712492211
RandomIdTool.getLowercaseId();              // 输出格式：ueppklqjsbqsxfhdlyye
RandomIdTool.getLowercaseId(10);            // 输出格式：airedlhfxc
RandomIdTool.getUppercaseId();              // 输出格式：PTKYKLDTLCKYLWAIARUF
RandomIdTool.getUppercaseId(10);            // 输出格式：RYFZLCXKAT
RandomIdTool.getAlphaNumeric(10);           // 输出格式：b1LQh8QsNxL15DKEE3yS
RandomIdTool.getAlphaNumericWithSpecial();  // 输出格式：_bl+Cbf0[Rrj:ta=KZWb
```

### 2.16、Captcha模块

一个验证码工具，支持字符验证码、算式验证码、中文验证码等多形式。支持自定义验证码生成算法、宽高、颜色、文字字体/大小/间距、背景颜色、边框宽度/边框、干扰策略…等。

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.captcha.CaptchaToolTest

常规使用方式：
```
// a、定义 CaptchaTool
CaptchaTool captchaTool = CaptchaTool.build();

// b、验证码文本生成
CaptchaTool.TextResult textResult = captchaTool.createText();
logger.info("验证码文本: {}", textResult.getText());
logger.info("验证码结果（可选，支持算式验证码）: {}", textResult.getResult());

// c、验证码图片生成
BufferedImage image = captchaTool.createImage(textResult);

// d、保存验证码图片
// 方式1：本地保存
ImageIO.write(image, "png", new FileOutputStream("/Users/admin/Downloads/captcha/captcha-1.png"));
// 方式2：Web接口（通过HttpServletResponse）返回
response.setContentType("image/png");
response.setHeader("Cache-Control", "no-cache");
ImageIO.write(image, "png", response.getOutputStream());
```

验证码初始化配置方式：
```
// 字符验证码，默认
CaptchaTool captchaTool = CaptchaTool.build();
// 字符验证码，自定义长度
CaptchaTool captchaTool = CaptchaTool.build().setTextCreator(new CaptchaTool.DefaultTextCreator(6));
// 字符验证码，中文汉字
CaptchaTool captchaTool = CaptchaTool.build().setTextCreator(new CaptchaTool.DefaultTextCreator("物华天宝人杰地灵山清水秀景色宜人"));
// 算式验证码
CaptchaTool captchaTool = CaptchaTool.build().setTextCreator(new CaptchaTool.ArithmeticTextCreator());
```

验证码设置参数：
```
CaptchaTool captchaTool = CaptchaTool.build()
    .setTextCreator(new CaptchaTool.DefaultTextCreator(6))      // 验证码内容生成组件，支持扩展，默认提供：DefaultTextCreator（字符）、ArithmeticTextCreator（算式）
    .setWidth(180)                                              // 验证码图片宽度
    .setHeight(60)                                              // 验证码图片高度
    .setColors(Arrays.asList(                                   // 验证码图片颜色；如配置多个，验证码生成时随机获取
            new Color(0xb83b5e),
            new Color(0xf08a5d),
            new Color(0xff9a00),
            new Color(0x00b8a9),
            new Color(0x004a7c),
            new Color(0x3d84a8),
            new Color(0x521262)
    ))
    .setFontSize(40)                                            // 验证码字体大小
    .setFonts(Arrays.asList(                                    // 验证码字体；如配置多个，验证码生成时随机获取
            new Font("Arial", Font.BOLD, 40),
            new Font("Courier", Font.BOLD, 40)
    ))
    .setCharSpace(8)                                            // 验证码字符间距
    .setBackgroundColorFrom(Color.LIGHT_GRAY)                   // 验证码背景颜色        
    .setBackgroundColorTo(Color.WHITE)                          // 验证码背景颜色，如果From和To背景颜色不一致，会生成渐变颜色；
    .setIsBorderDrawn(true)                                     // 验证码是否绘制边框
    .setBorderColor(Color.WHITE)                                // 验证码边框颜色
    .setBorderThickness(1)                                      // 验证码边框宽度
    .setNoiseColor(Color.WHITE)                                 // 验证码干扰线颜色       
    .setDistortedEngines(Arrays.asList(                         // 验证码干扰组件
            new CaptchaTool.NoneDistorted(),                    // 无干扰
            new CaptchaTool.ShadowDistorted(),                  // 阴影效果
            new CaptchaTool.WaterRippleDistorted(),             // 水波纹效果
            new CaptchaTool.FishEyeDistorted(),                 // 鱼眼效果
            new CaptchaTool.RippleDistorted()                   // 波纹效果
    ));
```

### 2.17、IO 模块

FileTool：一个高性能 File/文件 操作工具，支持丰富文件操作API；针对大文件读写设计分批操作、流式读写能力，降低内存占用、提升文件操作性能。

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.io.FileToolTest

```
FileTool.createFile(testFile);                            // 创建文件
FileTool.createDirectories(testFilePath);                 // 创建目录
FileTool.createParentDirectories(testFile);               // 创建文件父目录
      
FileTool.isFile(testFile);                                // 判断文件
FileTool.isDirectory(testFilePath);                       // 判断目录     
FileTool.isSameFile(file1, file2);                        // 判断文件是否相同                     
FileTool.exists(testFile);                                // 判断文件是否存在
      
FileTool.size(testFile);                                  // 获取文件 或 目录大小
FileTool.totalLines(testFile);                            // 获取文件行数
      
FileTool.delete(testFile);                                // 删除文件或目录
FileTool.clean(testFilePath);                             // 清空目录
      
FileTool.copy(src, dest);                                 // 复制文件
FileTool.move(src, dest);                                 // 移动文件或目录

FileTool.writeString(testFilePath, content);                       // 写入文件数据
FileTool.writeLines(testFilePath, Iterable<?> lines);              // 写入文件行数据
FileTool.writeLines(testFilePath, Supplier<?> lineSupplier);       // 写入文件数据，以迭代方式、流式写入，避免内存溢出

FileTool.readString(testFilePath);                                 // 读取文件数据
FileTool.readLines(testFilePath);                                  // 读取文件行数据
FileTool.readLines(testFilePath, Consumer<String> lineConsumer);   // 读取文件行数据，以迭代方式、流式读取，避免内存溢出
...
```

IOTool：IO工具，提供丰富IO读写操作能力

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.io.IOToolTest

```
IOTool.copy(inputStream, outputStream);                   // 拷贝流
IOTool.close(closeable);                                  // 关闭流
IOTool.readBytes(inputStream);                            // 读取字节数组
IOTool.writeString(testData, outputStream);               // 写入字节数组
...
```

### 2.18、DataStructure模块

BloomFilter：布隆过滤器，一种基于多哈希函数和位数组的概率型数据结构，具有高效空间利用与快速查询特性；

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.datastructure.BloomFilterTest;

```
// 1、初始化 BloomFilter
int size = 1000000;     // 1、容量
double fpp = 0.01;      // 2、误判率
BloomFilter<Long> bloomFilter = BloomFilter.create(Funnels.LONG, size, fpp);

// 2、添加元素
bloomFilter.put(999L);

// 3、判定元素是否存在
bloomFilter.mightContain(999L);
```

Trie：前缀数，一种哈希树的变种，利用公共前缀来节省存储空间和提高查询效率；

**代码示例**： 参考单元测试，见目录：com.xxl.tool.test.datastructure.TrieTest;

```
// 1、初始化 前缀树
Trie trie = new Trie();

// 2、插入单词
trie.insert("apple");

// 3、查询完整单词
trie.search("app");

// 4、前缀匹配检查
trie.startsWith("app");
```

### 2.19、更多
略


## 三、版本更新日志
### v1.0.0 Release Notes[2017-09-13]
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；

### v1.1.0 Release Notes[2017-12-14]
- 1、字段支持Date类型。至此，已经支持全部基础数据类型。
- 2、Java转换Excel时，字段类型改为从Field上读取，避免Value为空时空指针问题。
- 3、升级POI至3.17版本；
- 4、支持设置Field宽度；如果不指定列宽，将会自适应调整宽度；
- 5、多Sheet导出：导出Excel时支持设置多张sheet；
- 6、多Sheet导入：导入Excel时支持设置多张sheet，通过 "@ExcelSheet.name" 注解匹配Sheet;

### v1.1.1 Release Notes[2018-10-24]
- 1、支持设置Field水平位置，如居中、居左；
- 2、底层API优化，预约多Sheet操作支持；
- 3、空Cell导入抛错问题修复；
- 4、Cell数据类型识别优化，全类型支持；
- 5、导入时支持空Excel；导出时限制非空，否则无法进行类型推导。

### v1.2.0 Release Notes[2020-04-16]
- 将 XXL-EXCEL 和 XXL-Emoji 两个单独项目，统一合并至 XXL-TOOL，方便统一迭代维护； 
- excel模块：
    - 1、Excel 多版本导入导出兼容支持，包括：HSSFWorkbook=2003/xls、XSSFWorkbook=2007/xlsx ；
    - 2、升级POI至4.1.2版本；
- emoji模块：
    - 1、json组件调整为调整为gson；

### v1.3.0 Release Notes[2024-06-09]
- 1、开源协议变更，由 GPLv3 调整为 Apache2.0 开源协议；
- 2、新增Response模块，统一响应数据结构体，标准化数据结构、状态码等，降低协作成本；
- 3、新增Pipeline模块，高扩展性流程编排引擎；
- 4、新增Freemarker模块，模板引擎工具，支持根据模板文件生成文本、生成文件…等。

### v1.3.1 Release Notes[2024-11-09]
- 1、【强化】已有工具能力完善，包括：StringTool、GsonTool 等；
- 2、【新增】新增多个工具类模块，包括：FtlTool、CookieTool、PageModel、CacheTool、StreamTool 等；
- 3、【完善】工具类单测完善；
- 4、【升级】升级依赖版本，如slf4j、poi、spring、gson…等。

### v1.3.2 Release Notes[2024-12-29]
- 1、【新增】新增多个工具类模块，包括：Md5Tool、HexTool、HttpTool 等；
- 2、【完善】工具类单测完善；
- 3、【升级】升级依赖版本，如freemarker、junit…等。

### v1.4.0 Release Notes[2025-05-01]
- 1、【新增】JsonRpc模块：一个轻量级、跨语言远程过程调用实现，基于json、http实现（从XXL-JOB底层通讯组件提炼抽象）。
- 2、【新增】Concurrent模块：一系列并发编程工具，具备良好的线程安全、高并发及高性能优势，包括MessageQueue（高性能内存队列，30W+ TPS）、CyclicThread（后台循环线程）、TimeWheel（时间轮组件）等。
- 3、【新增】Auth模块：一系列权限认证相关工具，包括JwtTool等。
- 4、【强化】已有工具能力完善，包括 CollectionTool、MapTool、HttpTool 等；
- 5、【升级】升级依赖版本，包括 slf4j、poi、spring、gson、junit等。

### v1.4.1 Release Notes[2025-05-11]
- 1、【优化】IPTool.getAvailablePort 重构，优化非法端口号校验、端口占用状态处理逻辑；
- 2、【强化】已有工具能力完善，包括：DateTool 等；
- 3、【新增】新增多个工具类模块，包括：PropTool 等；

### v1.4.2 Release Notes[2025-05-17]
- 1、【强化】强化 ExcelTool 工具：一个基于注解的 Excel 与 Java对象 相互转换及导入导出工具；一行代码完成Java对象和Excel之间的转换。
  - a、Excel转换注解强化，支持忽略期望不导出的列：@ExcelField.ignore 
  - b、Excel列数据 与 Java对象Field 映射逻辑强化，支持根据 fieldName 与 注解名称 匹配，不强要求字段顺序必须保持一致；
  - c、Excel读取逻辑优化，降低小概率文件释放延迟问题。
- 2、【新增】新增 CsvTool 工具，提供Csv文件读写操作能力
- 3、【强化】已有工具能力完善，包括：DateTool 等；

### v1.5.0 Release Notes[2025-07-13]
- 1、【新增】ID模块：提供ID生成能力，支持多种ID生成策略，如：UUID、Snowflake、Date、Random 等多种ID生成工具；
- 2、【新增】Serializer模块：提供序列化、反序列化能力，支持扩展多种序列化格式，如jdk、protobuf、hessian…等；
- 3、【强化】已有工具能力完善，包括：StringTool、CookieTool 等；
- 4、【升级】升级依赖版本；

### v1.5.1 Release Notes[2025-08-09]
- 1、【新增】Encrypt模块：工具类能力增强，如 Md5Tool、SHA256Tool 等，支持自定义加盐加密；
- 2、【新增】新增基础模块工具，包括：ClassTool、FileTool、ObjectTool、ReflectionTool、TypeTool 等；
- 3、【强化】已有工具能力完善，包括：IOTool 等，支持更安全、灵活进行数据流操作；

### v2.0.0 Release Notes[2025-08-09]
- 1、【升级】项目升级JDK17；
- 2、【升级】项目部分依赖升级，如jakarta，适配JDK17；

### v2.1.0 Release Notes[2025-09-13]
- 1、【新增】新增“令牌桶/限流器”工具，TokenBucket，支持 突发限流、预热限流 等模式；
- 2、【新增】新增“验证码”工具，CaptchaTool，支持字符验证码、算式验证码、中文验证码等多形式。支持自定义验证码生成算法、宽高、颜色、文字字体、文字大小、文字间距、背景颜色、边框宽度、边框颜色、干扰组件…等。
- 3、【新增】新增基础模块工具，包括：ArrayTool 等；
- 4、【强化】已有工具能力完善，包括：PropTool、StringTool 等;
- 5、【升级】升级依赖版本，包括gson、nimbus-jose-jwt、spring等。

### v2.2.0 Release Notes[2025-10-08]
- 1、【强化】缓存工具（CacheTool）重构升级，支持多种缓存策略及特性：
  - 多种缓存类型实现：FIFO、LFU、LRU、Unlimited...等多种实现；
  - 锁分桶设计：在保障缓存读写线程安全基础上，降低锁冲突几率，从而提升缓存性能；
  - 缓存过期策略：支持多种缓存过期策略，如 “写入后过期、访问后过期” 等；
  - 缓存定时清理：支持 定时清理 过期缓存数据，主动降低缓存占用空间；
  - 缓存加载器：支持自定义缓存加载器，更灵活进行数据预热、数据初始化等操作；
  - 缓存监听器：支持自定义缓存监听器，监听缓存数据变化，如缓存清理；
  - 缓存统计信息：支持统计缓存命中数、未命中数、缓存大小等信息；
- 2、【强化】Http工具（HttpTool）重构升级，支持多种请求策略及特性：
  - 规范Http请求参数：支持自定义 Url、Method、ContentType、Header、Cookie、ConnectTimeout、ReadTimeout、UseCaches 等；
  - 请求拦截器：支持自定义请求拦截器，对请求进行预处理、后处理操作；
  - 请求安全校验：支持自定义Http Authorization信息；
  - 请求数据传递：支持多种请求数据传递方式，包括Body、Form等；
  - 基于Java对象Http交互：Http请求提交入参、以及响应结果均支持Java对象，工具底层屏蔽json序列化/反序列化工作，提升开发效率与工具易用性；
  - 提供链式调用API，提升开发效率及体验；
- 3、【强化】Http工具（HttpTool）强化：支持接口代理模式方式的HTTP客户端配置及使用；
  ```
    DemoService demoService = HttpTool.createClient().proxy(DemoService.class);
    RespDTO result = demoService.demo();
  ```
- 4、【强化】JsonRpc优化：标准化错误响应结构体，兼容void接口返回类型，优化错误码定义以及异常处理逻辑；
- 5、【强化】已有工具能力完善，StringTool增加format、replace等方法；

### v2.3.0 Release Notes[2025-10-24]
- 1、【强化】FileTool 工具能力升级，支持“创建、删除、移动、复制、读写”等丰富文件操作API；
- 2、【性能】FileTool 性能升级，针对大文件读写设计分批操作、流式读写能力，降低内存占用、提升文件操作性能。
- 3、【优化】IOTool 代码结构优化，提升 性能、易用性和维护性；
- 4、【优化】字符串工具类优化，修正 isNumeric 方法行为；
- 5、【优化】ExcelTool 工具优化，新增文件写入前目录初始化以及文件覆盖检测逻辑；
- 6、【升级】升级依赖版本。

### v2.3.1 Release Notes[2025-10-26]
- 1、【优化】HttpTool 优化，支持默认User-Agent设置；
- 2、【优化】字符串工具类优化，新增 API 接口以及单元测试；
- 3、【优化】IPTool优化，新增Address生成以及可用端口探测生成API能力；

### v2.3.2 Release Notes[2025-11-09]
- 1、【优化】增强Gson工具类，支持Pretty格式输出；
- 2、【优化】PageModel优化，统一分页模型字段；
- 3、【优化】ResponseCode枚举扩展，标准化状态码定义；

### v2.4.0 Release Notes[2025-12-13]
- 1、【新增】BloomFilter（布隆过滤器）：一种基于多哈希函数和位数组的概率型数据结构，具有高效空间利用与快速查询特性；
- 2、【新增】Trie（前缀数）：一种哈希树的变种，利用公共前缀来节省存储空间和提高查询效率；
- 3、【新增】BeanTool 工具：支持 Bean&Map 转换、Bean对象复制 等能力；
- 4、【强化】ExcelTool 强化：支持流式Excel读取和写入，大数据量下提升操作性能；
- 5、【强化】ReflectionTool 工具强化：完善 Method、Field、Proxy 等相关工具化方法；
- 6、【优化】工具包结构调整，规范模块命名与包路径，涉及 json、crypto 模块；
- 7、【升级】升级多项maven依赖至较新版本，如 gson、spring、poi 等；

### v2.4.1 Release Notes[2025-12-20]
- 1、【优化】HttpTool 代理调用逻辑调整，排除Object类方法的代理调用；
- 2、【优化】ExcelTool 能力增强，支持枚举类型字段导入导出；
- 3、【优化】EmojiTool 优化表情数据加载与解析逻辑，压缩冗余配置降低包体积；

### v2.4.2 Release Notes[2025-12-28]
- 1、【优化】HttpClient中状态码处理优化，状态校验通过后再进行后处理；
- 2、【优化】完善单元测试用例；

### v2.5.0 Release Notes[2026-04-05]
- 1、【新增】RegexTool ：正则表达式工具，支持正则校验、正则匹配、匹配结果分组、正则内容替换、正则分割等功能；
- 2、【增强】FileTool 增强：提升工具健壮性；
- 2、【升级】升级多项maven依赖至较新版本，如 spring、junit、jwt 等；


### TODO LIST
- Excel模块
    - 1、自定义默认行高
    - 2、单表行数限制：2003/xls=65536，2007/xlsx=1048576；行数限制内进行性能测试和优化；
    - 3、Java已经支持全基础数据类型导入导出，但是Excel仅支持STRING类型CELL，需要字段属性支持定义CELL类型；
- emoji模块:升级Emoji版本至最新Release版本：Unicode Emoji 11.0；


## 四、其他

### 4.1 项目贡献
欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-tool/issues/) 讨论新特性或者变更。

### 4.2 用户接入登记
更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-tool/issues/1 ) 登记，登记仅仅为了产品推广。

### 4.3 开源协议和版权
产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。

- Licensed under the Apache License, Version 2.0.
- Copyright (c) 2015-present, xuxueli.

---
### 捐赠
XXL-TOOL 是一个开源且免费项目，其正在进行的开发完全得益于支持者的支持。开源不易，[前往赞助项目开发](https://www.xuxueli.com/page/donate.html )
