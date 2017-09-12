## 《Java对象和Excel转换工具XXL-EXCEL》

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-excel/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-excel/)
[![GitHub release](https://img.shields.io/github/release/xuxueli/xxl-excel.svg)](https://github.com/xuxueli/xxl-excel/releases)
[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)

## 一、简介

### 1.1 概述
XXL-EXCEL 是在 Java 对象和 Excel 文档之间进行转换的迅速而灵活的工具。

### 1.2 特性
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；


### 1.4 下载

#### 文档地址

- [中文文档](https://github.com/xuxueli/xxl-excel/blob/master/doc/XXL-EXCEL官方文档.md)

#### 源码仓库地址

源码仓库地址 | Release Download
--- | ---
[https://github.com/xuxueli/xxl-excel](https://github.com/xuxueli/xxl-excel) | [Download](https://github.com/xuxueli/xxl-excel/releases)  


#### 技术交流
- [社区交流](http://www.xuxueli.com/page/community.html)

### 1.5 环境
- JDK：1.7+


## 二、快速入门

### 2.1 引入maven依赖

```
<!-- http://repo1.maven.org/maven2/com/xuxueli/xxl-excel-core/ -->
<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-excel</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2.2 Excel导出：Object 转换为 Excel

```
// 参考测试代码：com.xuxueli.poi.excel.test.Test

// Mock数据，Java对象列表
List<ShopDTO> shopDTOList = new ArrayList<ShopDTO>();
for (int i = 0; i < 100; i++) {
    ShopDTO shop = new ShopDTO(i, "商户"+i);
    shopDTOList.add(shop);
}

// Excel导出：Object 转换为 Excel
String filePath = "/Users/xuxueli/Downloads/demo-sheet.xls";
ExcelExportUtil.exportToFile(shopDTOList, filePath);

```

### 2.3 Excel导入：Excel 转换为 Object

```
// 参考测试代码：com.xuxueli.poi.excel.test.Test

// Excel导入：Excel 转换为 Object
List<Object> list = ExcelImportUtil.importExcel(ShopDTO.class, filePath);
System.out.println(list);
```


## 三、总体设计
### 3.1 功能定位

XXL-EXCEL 是在 Java 对象和 Excel 文档之间进行转换的迅速而灵活的工具。

### 3.2 Java 对象 和 Excel映射关系

-- | Excel | Java 对象
--- | --- | ---
表 | Sheet | Java对象列表
表头 | Sheet首行 | Java对象Field
数据 | Sheet一行记录 | Java对象列表中一个元素

### 3.3 核心注解：ExcelSheet

功能：描述Sheet信息，注解添加在待转换为Excel的Java对象类上，可选属性如下。

ExcelSheet | 说明
--- | ---
name | 表/Sheet名称
headColor | 表头/Sheet首行的颜色

### 3.4 核心注解：ExcelField

功能：描述Sheet的列信息，注解添加在待转换为Excel的Java对象类的字段上，可选属性如下。

ExcelField | 说明
--- | ---
name | 属性/列名称



## 四、版本更新日志
### 4.1 版本 V1.1.x，新特性[2015-12-05]
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；


### TODO LIST
- 1、单个Excel多Sheet导出导出；
- 2、列合并导入导出；
- 3、行合并导入导出；
- 4、HSSFWorkbook=2003/xls、XSSFWorkbook=2007/xlsx 兼容支持；

## 五、其他

### 5.1 项目贡献
欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-excel/issues/) 讨论新特性或者变更。

### 5.2 用户接入登记
更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-excel/issues/2 ) 登记，登记仅仅为了产品推广。

### 5.3 开源协议和版权
产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。

- Licensed under the GNU General Public License (GPL) v3.
- Copyright (c) 2015-present, xuxueli.

---
### 捐赠
无论金额多少都足够表达您这份心意，非常感谢 ：）    [XXL系列捐赠记录](http://www.xuxueli.com/page/donate.html )

微信：<img src="https://raw.githubusercontent.com/xuxueli/xxl-job/master/doc/images/donate-wechat.png" width="200">
支付宝：<img src="https://raw.githubusercontent.com/xuxueli/xxl-job/master/doc/images/donate-alipay.jpg" width="200">
