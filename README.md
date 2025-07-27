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

XXL-TOOL 是一个Java工具类库，致力于让Java开发更高效。包含 “日期、集合、字符串、IO、缓存、并发、Excel、Emoji、Response、Pipeline、Http、Json、JsonRpc、Encrypt、Auth、ID、Serializer...” 等数十个模块。


## Documentation
- [中文文档](https://www.xuxueli.com/xxl-tool/)


## Communication
- [社区交流](https://www.xuxueli.com/page/community.html)


## Modules
 模块                | 说明
|-------------------| -----------
 Core模块            | 包含集合、缓存、日期……等基础组件工具。
 IO模块              | 一系列处理IO（输入/输出）操作的工具。
 Concurrent模块      | 一系列并发编程工具，具备良好的线程安全、高并发及高性能优势，包括MessageQueue（高性能内存队列，30W+ TPS）、CyclicThread（后台循环线程）、TimeWheel（时间轮组件）等。
 Http模块            | 一系列处理Http通讯、IP、Cookie等相关工具。
 Json模块            | json序列化、反序列化工具封装，基于Gson。
 JsonRpc模块         | 一个轻量级、跨语言远程过程调用实现，基于json、http实现（对比传统RPC框架：[XXL-RPC](https://github.com/xuxueli/xxl-rpc)）。
 Excel模块           | 一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel之间的转换。
 Emoji模块           | 一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码。
 Response模块        | 统一响应数据结构体，标准化数据结构、状态码等，降低协作成本。
 Pipeline模块        | 高扩展性流程编排引擎。
 Exception模块       | 异常处理相关工具。
 Freemarker模块      | 模板引擎工具，支持根据模板文件实现 动态文本生成、静态文件生成 等，支持邮件发送、网页静态化场景。
 Encrypt模块         | 一系列处理编解码、加解密的工具，包括 Md5Tool、SHA256Tool、HexTool、Base64Tool...等。
 Auth模块            | 一系列权限认证相关工具，包括JwtTool...等。
 ID模块              | 一系列ID生成工具，支持多种ID生成策略，包括 UUID、Snowflake、Date、Random 等。
 Serializer模块      | 一系列序列化、反序列化工具，支持扩展多种序列化格式，包括 jdk、protobuf、hessian 等。
 ...               | ...


## Development
XXL-TOOL 前身为  XXL-EXCEL、XXL-EMOJI 两个独立项目，以及 XXL-JOB 内部经过验证的成熟工具类等，经过整合演进最终诞生。
- 1、XXL-EXCEL：首版发布于2017年9月，一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel文档之间的转换。同时保证性能和稳定。（已废弃，整合至 XXL-TOOL）
- 2、XXL-EMOJI：首版发布于2018年7月，一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码。（已废弃，整合至 XXL-TOOL）

## Contributing
Contributions are welcome! Open a pull request to fix a bug, or open an [Issue](https://github.com/xuxueli/xxl-tool/issues/) to discuss a new feature or change.

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-tool/issues/) 讨论新特性或者变更。

## 接入登记
Contributions are welcome! Open a pull request to fix a bug, or open an [Issue](https://github.com/xuxueli/xxl-tool/issues/) to discuss a new feature or change.

更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-tool/issues/1 ) 登记，登记仅仅为了产品推广。


## Copyright and License
This product is open source and free, and will continue to provide free community technical support. Individual or enterprise users are free to access and use.

- Licensed under the Apache License, Version 2.0.
- Copyright (c) 2015-present, xuxueli.

产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。


## Donate
XXL-TOOL is an open source and free project, with its ongoing development made possible entirely by the support of these awesome backers.

XXL-TOOL 是一个开源且免费项目，其正在进行的开发完全得益于支持者的支持。开源不易，[前往赞助项目开发](https://www.xuxueli.com/page/donate.html )
