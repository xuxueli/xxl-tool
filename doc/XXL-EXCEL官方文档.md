## 《Java对象和Excel转换工具XXL-EXCEL》

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-excel/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-excel/)
[![GitHub release](https://img.shields.io/github/release/xuxueli/xxl-excel.svg)](https://github.com/xuxueli/xxl-excel/releases)
[![License](https://img.shields.io/badge/license-GPLv3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0.html)
[![donate](https://img.shields.io/badge/%24-donate-ff69b4.svg?style=flat-square)](https://www.xuxueli.com/page/donate.html)

## 一、简介

### 1.1 概述
XXL-EXCEL 是一个灵活的Java对象和Excel文档相互转换的工具。

一行代码完成Java对象和Excel之间的转换。

### 1.2 特性
- 1、Excel导出：支持Java对象装换为Excel，并且支持File、字节数组、Workbook等多种导出方式；
- 2、Excel导入：支持Excel转换为Java对象，并且支持File、InputStream、文件路径、Workbook等多种导入方式；
- 3、全基础数据类型支持：Excel的映射Java对象支持设置任意基础数据类型，将会自动完整值注入；
- 4、Field宽度自适应；
- 5、多Sheet导出：导出Excel时支持设置多张sheet；
- 6、多Sheet导入：导入Excel时支持设置多张sheet，通过 "@ExcelSheet.name" 注解匹配Sheet;

### 1.4 下载

#### 文档地址

- [中文文档](https://www.xuxueli.com/xxl-excel/)

#### 源码仓库地址

源码仓库地址 | Release Download
--- | ---
[https://github.com/xuxueli/xxl-excel](https://github.com/xuxueli/xxl-excel) | [Download](https://github.com/xuxueli/xxl-excel/releases)
[https://gitee.com/xuxueli0323/xxl-excel](https://gitee.com/xuxueli0323/xxl-excel) | [Download](https://gitee.com/xuxueli0323/xxl-excel/releases)  


#### 技术交流
- [社区交流](https://www.xuxueli.com/page/community.html)

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

### 2.2 定义Java对象
```
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

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

}
```

### 2.3 Excel导出：Object 转换为 Excel

```
// 参考测试代码：com.xuxueli.poi.excel.test.Test

/**
 * Excel导出：Object 转换为 Excel
 */
ExcelExportUtil.exportToFile(filePath, shopDTOList);

```

### 2.4 Excel导入：Excel 转换为 Object

```
// 参考测试代码：com.xuxueli.poi.excel.test.Test

/**
 * Excel导入：Excel 转换为 Object
  */
List<Object> list = ExcelImportUtil.importExcel(filePath, ShopDTO.class);
```


## 三、总体设计
### 3.1 功能定位

XXL-EXCEL 是在 Java 对象和 Excel 文档之间进行转换的迅速而灵活的工具。

借助 XXL-EXCEL，一个Java类对象可以完整描述一张Excel表，XXL-EXCEL 做的事情就是把Java对象映射成Excel文件，同时保证性能和稳定。

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
- 1、[ING] HSSFWorkbook=2003/xls、XSSFWorkbook=2007/xlsx 兼容支持；
- 2、[ING] Excel导入、导出时，CellType 全类型支持，如string、number、date等；

### TODO LIST
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


## 五、其他

### 5.1 项目贡献
欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/xuxueli/xxl-excel/issues/) 讨论新特性或者变更。

### 5.2 用户接入登记
更多接入的公司，欢迎在 [登记地址](https://github.com/xuxueli/xxl-excel/issues/1 ) 登记，登记仅仅为了产品推广。

### 5.3 开源协议和版权
产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。

- Licensed under the GNU General Public License (GPL) v3.
- Copyright (c) 2015-present, xuxueli.

---
### 捐赠
无论金额多少都足够表达您这份心意，非常感谢 ：）      [前往捐赠](https://www.xuxueli.com/page/donate.html )