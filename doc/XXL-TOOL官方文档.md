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
XXL-TOOL 是一个Java工具类库，致力于让Java开发更高效。包含 “日期、集合、字符串、IO、缓存、并发、Excel、Emoji、Response、Pipeline、Http、Json、JsonRpc、Encrypt、Auth、ID、Serializer...” 等数十个模块。

### 1.2 组件列表
| 模块               | 说明
|------------------| ---------------
| Core模块           | 包含集合、缓存、日期……等基础组件工具。
| IO模块             | 一系列处理IO（输入/输出）操作的工具。
| Concurrent模块     | 一系列并发编程工具，具备良好的线程安全、高并发及高性能优势，包括MessageQueue（高性能内存队列，30W+ TPS）、CyclicThread（后台循环线程）、TimeWheel（时间轮组件）等。
| Http模块           | 一系列处理Http通讯、IP、Cookie等相关工具。
| Json模块           | json序列化、反序列化工具封装，基于Gson。
| JsonRpc模块        | 一个轻量级、跨语言远程过程调用实现，基于json、http实现（对比传统RPC框架：[XXL-RPC](https://github.com/xuxueli/xxl-rpc)）。
| Excel模块          | 一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel之间的转换。
| Emoji模块          | 一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码。
| Response模块       | 统一响应数据结构体，标准化数据结构、状态码等，降低协作成本。
| Pipeline模块       | 高扩展性流程编排引擎。
| Exception模块      | 异常处理相关工具。
| Freemarker模块     | 模板引擎工具，支持根据模板文件实现 动态文本生成、静态文件生成 等，支持邮件发送、网页静态化场景。
| Encrypt模块        | 一系列处理编解码、加解密的工具，包括 Md5Tool、SHA256Tool、HexTool、Base64Tool...等。
| Auth模块           | 一系列权限认证相关工具，包括JwtTool...等。
| ID模块             | 一系列ID生成工具，支持多种ID生成策略，包括 UUID、Snowflake、Date、Random 等。
| Serializer模块     | 一系列序列化、反序列化工具，支持扩展多种序列化格式，包括 jdk、protobuf、hessian 等。
| ...              | ...

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
- JDK：1.8+

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
| 模块           | 工具                 |  说明                                  
|--------------|--------------------|-------------------------------------|
| core         | AssertTool         | 断言工具，提供有效性校验能力
| core         | CollectionTool     | 集合工具，提供集合操作能力
| core         | DateTool           | 日期时间工具，提供日期时间转换及操作相关能力
| core         | MapTool            | Map 工具，提供Map操作能力
| core         | StringTool         | 字符串工具，提供字符串校验及操作相关能力
| auth         | JwtTool            | JWT工具，提供JWT生成及解析能力
| cache        | CacheTool          | 本地缓存工具，提供缓存操作能力
| concurrent   | CyclicThread       | 后台循环线程，支持精准、线程安全的周期性循环执行能力
| concurrent   | MessageQueue       | 高性能内存队列，单机支持 30W+ TPS
| concurrent   | TimeWheel          | 时间轮组件，提供定时任务执行能力
| emoji        | EmojiTool          | Emoji表情工具，提供Emoji表情编解码能力
| encrypt      | Base64Tool         | Base64工具，提供Base64编解码能力
| encrypt      | HexTool            | Hex工具，提供Hex编解码能力
| encrypt      | Md5Tool            | MD5工具，提供MD5编解码能力
| excel        | ExcelTool          | 一个基于注解的 Excel 与 Java对象 相互转换及导入导出工具；一行代码完成Java对象和Excel之间的转换。
| exception    | BizException       | 通用业务异常
| exception    | ThrowableTool      | 异常处理工具
| freemarker   | FtlTool            | 模板引擎工具, 支持根据模板文件实现 动态文本生成、静态文件生成 等，支持邮件发送、网页静态化场景。
| gson         | GsonTool           | Json序列化及反序列化工具，基于Gson
| http         | CookieTool         | Cookie工具，提供Cookie读写操作能力
| http         | HttpTool           | Http工具，提供Http通讯相关能力
| http         | IPTool             | IP工具，提供IP地址及端口号相关校验、生成及操作相关能力
| io           | IOTool             | IO工具，提供IO读写操作能力
| io           | CsvTool            | Csv工具，提供Csv文件读写操作能力
| jsonrpc      | JsonRpcClient      | 轻量级RPC通讯工具，客户端实现；基于json、http实现
| jsonrpc      | JsonRpcServer      | 轻量级RPC通讯工具，服务端实现；基于json、http实现
| pipeline     | PipelineExecutor   | Pipeline执行工具，提供pipeline注册管理以及执行相关能力
| pipeline     | Pipeline           | Pipeline工具，提供pipeline定义及执行相关能力
| response     | Response           | 标准响应结果封装，统一服务端数据返回格式
| response     | ResponseCode       | 标准响应码定义，统一服务端响应码体系
| response     | PageModel          | 标准分页结果封装，统一服务端分页数据格式


### 2.1、Core模块

参考单元测试，见目录：com.xxl.tool.test.core
```
// DateTool
String dateTimeStr = DateTool.formatDateTime(new Date());
Date date = DateTool.parseDateTime(dateTimeStr);

// CollectionTool
CollectionTool.isEmpty(list);
CollectionTool.union(a,b);
CollectionTool.intersection(a,b);
CollectionTool.disjunction(a,b);
CollectionTool.subtract(a,b);
CollectionTool.subtract(b,a);
CollectionTool.newArrayList();
CollectionTool.newArrayList(1,2,3);
CollectionTool.split(dataList, 50)  // 快速切割集合，每50条拆分一个集合

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

### 2.3、Response模块

参考单元测试：com.xxl.tool.test.response.ResponseBuilderTest
```
// 快速构建
Response<String> response1 = new ResponseBuilder<String>().success().build();
Response<Object> response2 = new ResponseBuilder<Object>().success().data("响应正文数据").build();
Response<String> response3 = new ResponseBuilder<String>().fail().build();
Response<String> response4 = new ResponseBuilder<String>().fail("错误提示消息").build();

// 完整构建
Response<String> response = new ResponseBuilder<String>()
                .code(ResponseCode.CODE_200.getCode())    // 状态码
                .msg("Sucess")                            // 提示消息
                .data("Hello World")                      // 响应正文数据
                .build();
```

### 2.4、Pipeline模块

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


### 2.5、Excel模块

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

### 2.6、Emoji模块

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

### 2.7、Freemarker 模块

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

### 2.8、Http 模块

参考单元测试，见目录：com.xxl.tool.test.http.HttpToolTest
```
// Http Post 请求
String resp = HttpTool.postBody("http://www.baidu.com/", "hello world");
String resp = HttpTool.postBody("http://www.baidu.com/", "hello world", 3000);
String resp = HttpTool.postBody("http://www.baidu.com/", "hello world", 3000, headers);
        
// Http Get 请求
String resp = HttpTool.get("http://www.baidu.com/");
String resp = HttpTool.get("http://www.baidu.com/", 3000);
String resp = HttpTool.get("http://www.baidu.com/", 3000, null);
```

### 2.9、IP 模块

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

### 2.10、JsonRpc  

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

### 2.11、Concurrent模块

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

### 2.12、Auth模块   
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

### 2.15、更多
略


## 三、版本更新日志
### 3.1 v1.0.0 Release Notes[2017-09-13]
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；

### 3.2 v1.1.0 Release Notes[2017-12-14]
- 1、字段支持Date类型。至此，已经支持全部基础数据类型。
- 2、Java转换Excel时，字段类型改为从Field上读取，避免Value为空时空指针问题。
- 3、升级POI至3.17版本；
- 4、支持设置Field宽度；如果不指定列宽，将会自适应调整宽度；
- 5、多Sheet导出：导出Excel时支持设置多张sheet；
- 6、多Sheet导入：导入Excel时支持设置多张sheet，通过 "@ExcelSheet.name" 注解匹配Sheet;

### 3.3 v1.1.1 Release Notes[2018-10-24]
- 1、支持设置Field水平位置，如居中、居左；
- 2、底层API优化，预约多Sheet操作支持；
- 3、空Cell导入抛错问题修复；
- 4、Cell数据类型识别优化，全类型支持；
- 5、导入时支持空Excel；导出时限制非空，否则无法进行类型推导。

### 3.4 v1.2.0 Release Notes[2020-04-16]
- 将 XXL-EXCEL 和 XXL-Emoji 两个单独项目，统一合并至 XXL-TOOL，方便统一迭代维护； 
- excel模块：
    - 1、Excel 多版本导入导出兼容支持，包括：HSSFWorkbook=2003/xls、XSSFWorkbook=2007/xlsx ；
    - 2、升级POI至4.1.2版本；
- emoji模块：
    - 1、json组件调整为调整为gson；

### 3.5 v1.3.0 Release Notes[2024-06-09]
- 1、开源协议变更，由 GPLv3 调整为 Apache2.0 开源协议；
- 2、新增Response模块，统一响应数据结构体，标准化数据结构、状态码等，降低协作成本；
- 3、新增Pipeline模块，高扩展性流程编排引擎；
- 4、新增Freemarker模块，模板引擎工具，支持根据模板文件生成文本、生成文件…等。

### 3.6 v1.3.1 Release Notes[2024-11-09]
- 1、【强化】已有工具能力完善，包括：StringTool、GsonTool 等；
- 2、【新增】新增多个工具类模块，包括：FtlTool、CookieTool、PageModel、CacheTool、StreamTool 等；
- 3、【完善】工具类单测完善；
- 4、【升级】升级依赖版本，如slf4j、poi、spring、gson…等。

### 3.7 v1.3.2 Release Notes[2024-12-29]
- 1、【新增】新增多个工具类模块，包括：Md5Tool、HexTool、HttpTool 等；
- 2、【完善】工具类单测完善；
- 3、【升级】升级依赖版本，如freemarker、junit…等。

### 3.8 v1.4.0 Release Notes[2025-05-01]
- 1、【新增】JsonRpc模块：一个轻量级、跨语言远程过程调用实现，基于json、http实现（从XXL-JOB底层通讯组件提炼抽象）。
- 2、【新增】Concurrent模块：一系列并发编程工具，具备良好的线程安全、高并发及高性能优势，包括MessageQueue（高性能内存队列，30W+ TPS）、CyclicThread（后台循环线程）、TimeWheel（时间轮组件）等。
- 3、【新增】Auth模块：一系列权限认证相关工具，包括JwtTool等。
- 4、【强化】已有工具能力完善，包括 CollectionTool、MapTool、HttpTool 等；
- 5、【升级】升级依赖版本，包括 slf4j、poi、spring、gson、junit等。

### 3.9 v1.4.1 Release Notes[2025-05-11]
- 1、【优化】IPTool.getAvailablePort 重构，优化非法端口号校验、端口占用状态处理逻辑；
- 2、【强化】已有工具能力完善，包括：DateTool 等；
- 3、【新增】新增多个工具类模块，包括：PropTool 等；

### 3.10 v1.4.2 Release Notes[2025-05-17]
- 1、【强化】强化 ExcelTool 工具：一个基于注解的 Excel 与 Java对象 相互转换及导入导出工具；一行代码完成Java对象和Excel之间的转换。
  - a、Excel转换注解强化，支持忽略期望不导出的列：@ExcelField.ignore 
  - b、Excel列数据 与 Java对象Field 映射逻辑强化，支持根据 fieldName 与 注解名称 匹配，不强要求字段顺序必须保持一致；
  - c、Excel读取逻辑优化，降低小概率文件释放延迟问题。
- 2、【新增】新增 CsvTool 工具，提供Csv文件读写操作能力
- 3、【强化】已有工具能力完善，包括：DateTool 等；

### 3.11 v1.5.0 Release Notes[2025-07-13]
- 1、【新增】ID模块：提供ID生成能力，支持多种ID生成策略，如：UUID、Snowflake、Date、Random 等多种ID生成工具；
- 2、【新增】Serializer模块：提供序列化、反序列化能力，支持扩展多种序列化格式，如jdk、protobuf、hessian…等；
- 3、【强化】已有工具能力完善，包括：StringTool、CookieTool 等；
- 4、【升级】升级依赖版本；

### 3.12 v1.5.1 Release Notes[迭代中]
- 1、【新增】Encrypt模块：工具类能力增强，如Md5Tool、SHA256Tool等，支持自定义加盐加密；
- 2、【Todo】Excel模块：流式导入导出；
- 3、【Todo】Excel模块：自定义默认行高；


### TODO LIST
- excel模块：大数据导出，流式导入导出；
- excel模块
    - 1、单个Excel多Sheet导出导出；
    - 2、列合并导入导出；
    - 3、行合并导入导出；
    - 4、同一个单元格，横向、竖向拆分多个单元格；List属性；
    - 5、流式导入：多批次导入数据；
    - 7、流式导出：分页方式导出数据；
    - 6、单表行数限制：2003/xls=65536，2007/xlsx=1048576；行数限制内进行性能测试和优化；
    - 8、排序的字段，对时间等其他类型的处理。
    - 9、Java已经支持全基础数据类型导入导出，但是Excel仅支持STRING类型CELL，需要字段属性支持定义CELL类型；
    - 10、Excel导入多Sheet支持，API 格式 "Map<String, List<Object>> importExcel(String filePath, Class<?> sheetClass ...)" ；
    - 11、Excel导入、导出时，CellType 全类型支持，如string、number、date等；
- emoji模块
    - 1、Emoji远程编解码服务；
    - 2、升级Emoji版本至最新Release版本：Unicode Emoji 11.0；


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
