<p align="center">
    <img src="https://www.xuxueli.com/doc/static/xxl-job/images/xxl-logo.jpg" width="150">
    <h3 align="center">XXL-TOOL</h3>
    <p align="center">
        XXL-TOOL, a series of tools that keep Java efficient.
        <br>
        <a href="https://www.xuxueli.com/xxl-tool/"><strong>-- Home Page --</strong></a>
        <br>
        <br>
        <a href="https://github.com/xuxueli/xxl-tool/actions">
            <img src="https://github.com/xuxueli/xxl-tool/workflows/Java%20CI/badge.svg" >
        </a>
        <a href="https://central.sonatype.com/artifact/com.xuxueli/xxl-tool">
            <img src="https://img.shields.io/maven-central/v/com.xuxueli/xxl-tool" >
        </a>
        <a href="https://github.com/xuxueli/xxl-tool/releases">
            <img src="https://img.shields.io/github/release/xuxueli/xxl-tool.svg" >
        </a>
        <a href="https://github.com/xuxueli/xxl-tool/">
            <img src="https://img.shields.io/github/stars/xuxueli/xxl-tool" >
        </a>
        <img src="https://img.shields.io/github/license/xuxueli/xxl-tool.svg" >
        <a href="https://www.xuxueli.com/page/donate.html">
            <img src="https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square" >
        </a>
    </p>    
</p>


## Introduction
XXL-Tool is a Java tool library that makes Java development more efficient. The tools include "date、collection、string、IO、cache、concurrency、Excel, Emoji、Pipeline、Http、Json、JsonRpc、Encrypt、Auth、ID、Serializer..." and so on.

XXL-TOOL 是一个Java工具类库，致力于让Java开发更高效。包含 “日期、集合、字符串、IO、缓存、并发、Excel、Emoji、Response、Pipeline、Http、Json、JsonRpc、Encrypt、Auth、ID、Serializer、验证码、限流器...” 等数十个模块。


## Documentation
- [中文文档](https://www.xuxueli.com/xxl-tool/)


## Communication
- [社区交流](https://www.xuxueli.com/page/community.html)


## Modules
| 模块                 | 说明
|--------------------| ---------------
| Core模块             | 包含 集合、缓存、日期、反射、断言、……等基础工具。
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


## Tool List
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


## Development
XXL-TOOL 前身为  XXL-EXCEL、XXL-EMOJI 两个独立项目，以及 XXL-JOB 内部经过验证的成熟工具类等，经过整合演进最终诞生。
- 1、XXL-EXCEL：首版发布于2017年9月，一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel文档之间的转换。同时保证性能和稳定。（已废弃，整合至 XXL-TOOL）
- 2、XXL-EMOJI：首版发布于2018年7月，一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码。（已废弃，整合至 XXL-TOOL）

## Contributing
Contributions are welcome! Open a pull request to fix a bug, or open an [Issue](https://github.com/xuxueli/xxl-tool/issues/) to discuss a new feature or change.

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-tool/issues/) 讨论新特性或者变更。

## 接入登记
More companies that have integrated are welcome to register at [registration link](https://github.com/xuxueli/xxl-tool/issues/1). Registration is solely for product promotion purposes.

更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-tool/issues/1 ) 登记，登记仅仅为了产品推广。


## Copyright and License
This product is open source and free, and will continue to provide free community technical support. Individual or enterprise users are free to access and use.

- Licensed under the Apache License, Version 2.0.
- Copyright (c) 2015-present, xuxueli.

产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。


## Donate
XXL-TOOL is an open source and free project, with its ongoing development made possible entirely by the support of these awesome backers.

XXL-TOOL 是一个开源且免费项目，其正在进行的开发完全得益于支持者的支持。开源不易，[前往赞助项目开发](https://www.xuxueli.com/page/donate.html )
