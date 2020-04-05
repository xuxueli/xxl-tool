## 《Java工具类库XXL-TOOL》

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-tool/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-tool/)
[![GitHub release](https://img.shields.io/github/release/xuxueli/xxl-tool.svg)](https://github.com/xuxueli/xxl-tool/releases)
[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![donate](https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square)](https://www.xuxueli.com/page/donate.html)

[TOCM]

[TOC]

## 一、简介

### 1.1 概述
XXL-TOOL 是一个Java工具类库，让Java开发更高效。包含 “集合、缓存、并发、字符串、IO、Excel、Emoji……” 等数十个模块。

### 1.2 组件列表
模块 | 说明
--- | ---
xxl-tool-core | 核心模块：包含集合、日期……等基础组件工具
xxl-tool-excel | Excel模块：一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel之间的转换。
xxl-tool-emoji | emoji模块：一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码.
... | ...

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


## 二、快速入门

### 2.1 引入maven依赖
可以根据需求对每个模块单独引入，也可以通过引入xxl-tool-all方式引入全部模块，方式如下：
```
<!-- http://repo1.maven.org/maven2/com/xuxueli/xxl-tool/ -->
<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-tool-all</artifactId>
    <version>${最新稳定版}</version>
</dependency>
```

### 2.2 xxl-tool-core模块
略

### 2.3 xxl-tool-excel模块
- maven依赖添加

- 定义Java对象
```java
@ExcelSheet(name = "商户列表", headColor = HSSFColor.HSSFColorPredefined.LIGHT_GREEN)
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

- Excel导出：Object 转换为 Excel

```java
// 参考测试代码：com.xxl.tool.excel.test.Test

/**
 * Excel导出：Object 转换为 Excel
 */
ExcelExportUtil.exportToFile(filePath, shopDTOList);

```

- Excel导入：Excel 转换为 Object

```
// 参考测试代码：com.xxl.tool.excel.test.Test

/**
 * Excel导入：Excel 转换为 Object
  */
List<Object> list = ExcelImportUtil.importExcel(filePath, ShopDTO.class);
```

### 2.3 xxl-tool-emoji模块
- maven依赖添加

- 使用示例  

```java
// 参考测试代码：com.xxl.tool.emoji.test.EmojiTest

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

- 示例代码运行日志输入    

```text
aliases encode: 一朵美丽的茉莉:rose:
aliases decode: 一朵美丽的茉莉🌹

decimal encode: 一朵美丽的茉莉&#127801;
decimal decode: 一朵美丽的茉莉🌹

hexdecimal encode: 一朵美丽的茉莉&#x1f339;
hexdecimal decode: 一朵美丽的茉莉🌹
```

## 三、总体设计

### 3.1 xxl-tool-excel模块
##### 3.1.1 功能定位    
一个灵活的Java对象和Excel文档相互转换的工具。一行代码完成Java对象和Excel文档之间的转换。同时保证性能和稳定。

##### 3.1.2 特性
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；
- 3、全基础数据类型支持：Excel的映射Java对象支持设置任意基础数据类型，将会自动完整值注入；
- 4、Field宽度自适应；
- 5、多Sheet导出：导出Excel时支持设置多张sheet；
- 6、多Sheet导入：导入Excel时支持设置多张sheet，通过 "@ExcelSheet.name" 注解匹配Sheet;

##### 3.1.3 Java 对象 和 Excel映射关系

-- | Excel | Java 对象
--- | --- | ---
表 | Sheet | Java对象列表
表头 | Sheet首行 | Java对象Field
数据 | Sheet一行记录 | Java对象列表中一个元素

##### 3.1.4 核心注解：ExcelSheet

功能：描述Sheet信息，注解添加在待转换为Excel的Java对象类上，可选属性如下。

ExcelSheet | 说明
--- | ---
name | 表/Sheet名称
headColor | 表头/Sheet首行的颜色

##### 3.1.5 核心注解：ExcelField

功能：描述Sheet的列信息，注解添加在待转换为Excel的Java对象类的字段上，可选属性如下。

ExcelField | 说明
--- | ---
name | 属性/列名称

### 3.2 xxl-tool-emoji模块
##### 3.2.1 功能定位
XXL-EMOJI 是一个灵活可扩展的Emoji表情编解码库，可快速实现Emoji表情的编解码.

##### 3.2.2 特性
- 1、简洁：API直观简洁，一分钟上手；
- 2、易扩展：模块化的结构，可轻松扩展；
- 3、别名自定义：支持为Emoji自定义别名；
- 4、实时性：实时收录最新发布的Emoji；

##### 3.2.3 Emoji编码类型

概念 | 说明
--- | ---
EmojiEncode.ALIASES | 将Emoji表情转换为别名，格式为 ": alias :"；
EmojiEncode.HTML_DECIMAL | 将Emoji表情Unicode数据转换为十进制数据；
EmojiEncode.HTML_HEX_DECIMAL | 将Emoji表情Unicode数据转换为十六进制数据；

##### 3.2.4 Emoji编解码API

API | 说明
--- | ---
public static String encodeUnicode(String input, EmojiTransformer transformer, FitzpatrickAction fitzpatrickAction) | Emoji表情编码方法，支持自定义编码逻辑；
public static String encodeUnicode(String input, EmojiEncode emojiEncode, FitzpatrickAction fitzpatrickAction) | Emoji表情编码方法，支持自定义编码类型；
public static String encodeUnicode(String input, EmojiEncode emojiEncode) | Emoji表情编码方法，支持自定义编码类型；
public static String encodeUnicode(String input) | Emoji表情编码方法，编码类型默认为 "ALIASES" ；
public static String decodeToUnicode(String input) | Emoji表情解码方法，支持针对 "ALIASES、HTML_DECIMAL、HTML_HEX_DECIMAL" 等编码方式解码；
public static String removeEmojis(String input, final Collection<Emoji> emojisToRemove, final Collection<Emoji> emojisToKeep) | 清除输入字符串中的Emoji数据；
public static List<String> findEmojis(String input) | 查找输入字符转中的全部Emoji数据列表；

##### 3.2.5 自定义Emoji别名
略


## 四、版本更新日志
### 版本 V1.0.0，新特性[2017-09-13]
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；

### 版本 V1.1.0，新特性[2017-12-14]
- 1、字段支持Date类型。至此，已经支持全部基础数据类型。
- 2、Java转换Excel时，字段类型改为从Field上读取，避免Value为空时空指针问题。
- 3、升级POI至3.17版本；
- 4、支持设置Field宽度；如果不指定列宽，将会自适应调整宽度；
- 5、多Sheet导出：导出Excel时支持设置多张sheet；
- 6、多Sheet导入：导入Excel时支持设置多张sheet，通过 "@ExcelSheet.name" 注解匹配Sheet;

### 版本 V1.1.1，新特性[2018-10-24]
- 1、支持设置Field水平位置，如居中、居左；
- 2、底层API优化，预约多Sheet操作支持；
- 3、空Cell导入抛错问题修复；
- 4、Cell数据类型识别优化，全类型支持；
- 5、导入时支持空Excel；导出时限制非空，否则无法进行类型推导。

### 版本 v1.1.2，新特性[迭代中]
- 1、[ING]【excel模块】 HSSFWorkbook=2003/xls、XSSFWorkbook=2007/xlsx 兼容支持；
- 2、[ING]【excel模块】Excel导入、导出时，CellType 全类型支持，如string、number、date等；
- 3、【emoji模块】升级Emoji版本至最新Release版本：Unicode Emoji 11.0；
- 4、【emoji模块】精简配置文件，体积减少100k,；

### TODO LIST
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
- emoji模块
    - 1、Emoji远程编解码服务；


## 五、其他

### 5.1 项目贡献
欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-tool/issues/) 讨论新特性或者变更。

### 5.2 用户接入登记
更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-tool/issues/1 ) 登记，登记仅仅为了产品推广。

### 5.3 开源协议和版权
产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。

- Licensed under the GNU General Public License (GPL) v3.
- Copyright (c) 2015-present, xuxueli.

---
### 捐赠
无论金额多少都足够表达您这份心意，非常感谢 ：）      [前往捐赠](https://www.xuxueli.com/page/donate.html )