package com.xxl.tool.test.core;

import com.xxl.tool.core.RegexTool;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RegexTool 单元测试
 *
 * @author xuxueli 2026-04-05
 */
public class RegexToolTest {

    // ==================== matches 方法测试 ====================

    @Test
    public void test_matches() {
        // 1. 基本匹配测试 - 简单字符串
        assertTrue(RegexTool.matches("hello", "hello"));
        assertFalse(RegexTool.matches("hello", "world"));

        // 2. 正则表达式匹配 - 数字
        assertTrue(RegexTool.matches("\\d+", "12345"));
        assertFalse(RegexTool.matches("\\d+", "abc"));
        assertFalse(RegexTool.matches("\\d+", "123abc"));

        // 3. 正则表达式匹配 - 邮箱格式
        assertTrue(RegexTool.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "test@example.com"));
        assertFalse(RegexTool.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "invalid-email"));

        // 4. 正则表达式匹配 - 手机号
        assertTrue(RegexTool.matches("1[3-9]\\d{9}", "13812345678"));
        assertFalse(RegexTool.matches("1[3-9]\\d{9}", "12345678901"));

        // 5. 特殊字符匹配
        assertTrue(RegexTool.matches(".*", ""));
        assertTrue(RegexTool.matches(".*", "anything"));
        assertTrue(RegexTool.matches(".+", "a"));
        assertFalse(RegexTool.matches(".+", ""));

        // 6. 边界匹配
        assertTrue(RegexTool.matches("^hello$", "hello"));
        assertFalse(RegexTool.matches("^hello$", "hello world"));

        // 7. 空值和空白字符串测试
        assertFalse(RegexTool.matches(null, "test"));
        assertFalse(RegexTool.matches("", "test"));
        assertFalse(RegexTool.matches("   ", "test"));
        assertFalse(RegexTool.matches("test", null));
        assertFalse(RegexTool.matches("test", ""));
        assertFalse(RegexTool.matches("test", "   "));
        assertFalse(RegexTool.matches(null, null));

        // 8. 无效正则表达式（应返回 false 而不是抛出异常）
        assertFalse(RegexTool.matches("[invalid", "test"));
        assertFalse(RegexTool.matches("*", "test"));
        assertFalse(RegexTool.matches("(?", "test"));

        // 9. 大小写敏感测试
        assertTrue(RegexTool.matches("ABC", "ABC"));
        assertFalse(RegexTool.matches("ABC", "abc"));
        assertTrue(RegexTool.matches("(?i)ABC", "abc"));

        // 10. 中文字符匹配
        assertTrue(RegexTool.matches("[\u4e00-\u9fa5]+", "你好世界"));
        assertFalse(RegexTool.matches("[\u4e00-\u9fa5]+", "hello"));
    }

    // ==================== findAll 方法测试 ====================

    @Test
    public void test_findAll() {
        // 1. 基本查找 - 单个匹配
        List<String> result1 = RegexTool.findAll("\\d+", "abc123def");
        assertEquals(1, result1.size());
        assertEquals("123", result1.get(0));

        // 2. 多个匹配
        List<String> result2 = RegexTool.findAll("\\d+", "abc123def456ghi789");
        assertEquals(3, result2.size());
        assertEquals("123", result2.get(0));
        assertEquals("456", result2.get(1));
        assertEquals("789", result2.get(2));

        // 3. 无匹配情况
        List<String> result3 = RegexTool.findAll("\\d+", "abcdefghi");
        assertTrue(result3.isEmpty());

        // 4. 字母匹配
        List<String> result4 = RegexTool.findAll("[a-z]+", "ABC123def456ghi789");
        assertEquals(2, result4.size());
        assertEquals("def", result4.get(0));
        assertEquals("ghi", result4.get(1));

        // 5. 单词匹配
        List<String> result5 = RegexTool.findAll("\\w+", "hello world 123");
        assertEquals(3, result5.size());
        assertEquals("hello", result5.get(0));
        assertEquals("world", result5.get(1));
        assertEquals("123", result5.get(2));

        // 6. 特殊模式匹配 - 邮箱
        List<String> result6 = RegexTool.findAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", 
                "Contact us at test@example.com or support@test.org");
        assertEquals(2, result6.size());
        assertEquals("test@example.com", result6.get(0));
        assertEquals("support@test.org", result6.get(1));

        // 7. 空值和空白字符串测试
        assertTrue(RegexTool.findAll(null, "test").isEmpty());
        assertTrue(RegexTool.findAll("", "test").isEmpty());
        assertTrue(RegexTool.findAll("   ", "test").isEmpty());
        assertTrue(RegexTool.findAll("test", null).isEmpty());
        assertTrue(RegexTool.findAll("test", "").isEmpty());
        assertTrue(RegexTool.findAll("test", "   ").isEmpty());
        assertTrue(RegexTool.findAll(null, null).isEmpty());

        // 8. 无效正则表达式（应返回空列表而不是抛出异常）
        assertTrue(RegexTool.findAll("[invalid", "test").isEmpty());
        assertTrue(RegexTool.findAll("*", "test").isEmpty());

        // 9. 重叠匹配测试
        List<String> result9 = RegexTool.findAll("aa", "aaa");
        assertEquals(1, result9.size()); // 默认不重叠匹配

        // 10. 贪婪与非贪婪匹配
        List<String> result10a = RegexTool.findAll("<.*>", "<div><span>text</span></div>");
        assertEquals(1, result10a.size());
        assertEquals("<div><span>text</span></div>", result10a.get(0));

        List<String> result10b = RegexTool.findAll("<.*?>", "<div><span>text</span></div>");
        assertEquals(4, result10b.size());
        assertEquals("<div>", result10b.get(0));
        assertEquals("<span>", result10b.get(1));
        assertEquals("</span>", result10b.get(2));
        assertEquals("</div>", result10b.get(3));
    }

    // ==================== extract 方法测试 ====================

    @Test
    public void test_extract() {
        // 1. 提取分组 - 基本分组 (group=1)
        List<String> result1 = RegexTool.extract("(\\d+)-(\\d+)", "phone: 123-456", 1);
        assertEquals(1, result1.size());
        assertEquals("123", result1.get(0));

        // 2. 提取第二个分组 (group=2)
        List<String> result2 = RegexTool.extract("(\\d+)-(\\d+)", "phone: 123-456", 2);
        assertEquals(1, result2.size());
        assertEquals("456", result2.get(0));

        // 3. 提取完整匹配 (group=0)
        List<String> result3 = RegexTool.extract("(\\d+)-(\\d+)", "phone: 123-456", 0);
        assertEquals(1, result3.size());
        assertEquals("123-456", result3.get(0));

        // 4. 多个匹配中提取分组
        List<String> result4 = RegexTool.extract("(\\w+)@(\\w+\\.\\w+)", "Email: user1@gmail.com and user2@yahoo.com", 1);
        assertEquals(2, result4.size());
        assertEquals("user1", result4.get(0));
        assertEquals("user2", result4.get(1));

        // 5. 提取域名部分
        List<String> result5 = RegexTool.extract("(\\w+)@(\\w+\\.\\w+)", "Email: user1@gmail.com and user2@yahoo.com", 2);
        assertEquals(2, result5.size());
        assertEquals("gmail.com", result5.get(0));
        assertEquals("yahoo.com", result5.get(1));

        // 6. 日期提取
        List<String> result6 = RegexTool.extract("(\\d{4})-(\\d{2})-(\\d{2})", "Date: 2024-01-15 and 2023-12-25", 1);
        assertEquals(2, result6.size());
        assertEquals("2024", result6.get(0));
        assertEquals("2023", result6.get(1));

        List<String> result6b = RegexTool.extract("(\\d{4})-(\\d{2})-(\\d{2})", "Date: 2024-01-15 and 2023-12-25", 2);
        assertEquals(2, result6b.size());
        assertEquals("01", result6b.get(0));
        assertEquals("12", result6b.get(1));

        List<String> result6c = RegexTool.extract("(\\d{4})-(\\d{2})-(\\d{2})", "Date: 2024-01-15 and 2023-12-25", 3);
        assertEquals(2, result6c.size());
        assertEquals("15", result6c.get(0));
        assertEquals("25", result6c.get(1));

        // 7. 分组索引超出范围（应跳过该匹配）
        List<String> result7 = RegexTool.extract("(\\d+)", "test123", 5);
        assertTrue(result7.isEmpty());

        // 8. 空值和空白字符串测试
        assertTrue(RegexTool.extract(null, "test", 0).isEmpty());
        assertTrue(RegexTool.extract("", "test", 0).isEmpty());
        assertTrue(RegexTool.extract("   ", "test", 0).isEmpty());
        assertTrue(RegexTool.extract("test", null, 0).isEmpty());
        assertTrue(RegexTool.extract("test", "", 0).isEmpty());
        assertTrue(RegexTool.extract("test", "   ", 0).isEmpty());
        assertTrue(RegexTool.extract(null, null, 0).isEmpty());

        // 9. 无效的 group 参数（负数应抛出异常）
        assertThrows(IllegalArgumentException.class, () -> {
            RegexTool.extract("(\\d+)", "test123", -1);
        });

        // 10. 无效正则表达式（应返回空列表）
        assertTrue(RegexTool.extract("[invalid", "test", 0).isEmpty());

        // 11. URL 提取示例
        List<String> result11 = RegexTool.extract("https?://([^/]+)", "Visit https://www.example.com/path or http://test.org/page", 1);
        assertEquals(2, result11.size());
        assertEquals("www.example.com", result11.get(0));
        assertEquals("test.org", result11.get(1));

        // 12. 命名分组不支持时的处理（使用数字分组）
        List<String> result12 = RegexTool.extract("name=(\\w+)&age=(\\d+)", "user: name=John&age=25", 1);
        assertEquals(1, result12.size());
        assertEquals("John", result12.get(0));
    }

    // ==================== replaceAll 方法测试 ====================

    @Test
    public void test_replaceAll() {
        // 1. 基本替换
        String result1 = RegexTool.replaceAll("world", "hello world", "universe");
        assertEquals("hello universe", result1);

        // 2. 多个匹配替换
        String result2 = RegexTool.replaceAll("apple", "apple pie and apple juice", "orange");
        assertEquals("orange pie and orange juice", result2);

        // 3. 正则替换 - 数字替换
        String result3 = RegexTool.replaceAll("\\d+", "abc123def456", "#");
        assertEquals("abc#def#", result3);

        // 4. 正则替换 - 去除空格
        String result4 = RegexTool.replaceAll("\\s+", "hello   world  test", " ");
        assertEquals("hello world test", result4);

        // 5. 使用捕获组进行替换
        String result5 = RegexTool.replaceAll("(\\w+)@(\\w+\\.\\w+)",  "Email: user@example.com", "$1 at $2");
        assertEquals("Email: user at example.com", result5);

        // 6. 删除匹配的内容
        String result6 = RegexTool.replaceAll("\\d+", "abc123def456ghi", "");
        assertEquals("abcdefghi", result6);

        // 7. 无匹配时保持原样
        String result7 = RegexTool.replaceAll("xyz", "hello world", "test");
        assertEquals("hello world", result7);

        // 8. 替换 HTML 标签
        String result8 = RegexTool.replaceAll("<[^>]+>", "<div>Hello <b>World</b></div>", "");
        assertEquals("Hello World", result8);

        // 9. 空值和 null 测试
        assertNull(RegexTool.replaceAll("test", null, "rep"));
        assertEquals("test input", RegexTool.replaceAll(null, "test input", "rep"));
        assertEquals("test input", RegexTool.replaceAll("", "test input", "rep"));
        assertEquals("test input", RegexTool.replaceAll("   ", "test input", "rep"));
        
        // replacement 为 null 时返回原字符串
        assertEquals("test input", RegexTool.replaceAll("test", "test input", null));

        // 10. 无效正则表达式（应返回原字符串）
        assertEquals("test input", RegexTool.replaceAll("[invalid", "test input", "rep"));
        assertEquals("test input", RegexTool.replaceAll("*", "test input", "rep"));

        // 11. 特殊字符替换
        String result11 = RegexTool.replaceAll("[.,!?]", "Hello, World! How are you?", " ");
        assertEquals("Hello  World  How are you ", result11);

        // 12. 大小写敏感替换
        String result12a = RegexTool.replaceAll("hello", "Hello HELLO hello", "hi");
        assertEquals("Hello HELLO hi", result12a);

        String result12b = RegexTool.replaceAll("(?i)hello", "Hello HELLO hello", "hi");
        assertEquals("hi hi hi", result12b);

        // 13. 替换为特殊字符
        String result13 = RegexTool.replaceAll("name", "Hello name", "\\$100");
        assertEquals("Hello $100", result13);

        // 14. 复杂正则替换 - 格式化电话号码
        String result14 = RegexTool.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "13812345678", "$1-$2-$3");
        assertEquals("138-1234-5678", result14);
    }

    // ==================== split 方法测试 ====================

    @Test
    public void test_split() {
        // 1. 基本分割 - 逗号
        List<String> result1 = RegexTool.split(",", "apple,banana,orange");
        assertEquals(3, result1.size());
        assertEquals("apple", result1.get(0));
        assertEquals("banana", result1.get(1));
        assertEquals("orange", result1.get(2));

        // 2. 分割 - 空格
        List<String> result2 = RegexTool.split("\\s+", "hello world test");
        assertEquals(3, result2.size());
        assertEquals("hello", result2.get(0));
        assertEquals("world", result2.get(1));
        assertEquals("test", result2.get(2));

        // 3. 分割 - 多个分隔符
        List<String> result3 = RegexTool.split("[,;|]", "apple,banana;orange|grape");
        assertEquals(4, result3.size());
        assertEquals("apple", result3.get(0));
        assertEquals("banana", result3.get(1));
        assertEquals("orange", result3.get(2));
        assertEquals("grape", result3.get(3));

        // 4. 分割 - 数字作为分隔符
        List<String> result4 = RegexTool.split("\\d+", "abc123def456ghi");
        assertEquals(3, result4.size());
        assertEquals("abc", result4.get(0));
        assertEquals("def", result4.get(1));
        assertEquals("ghi", result4.get(2));

        // 5. 分割 - 特殊字符
        List<String> result5 = RegexTool.split("[.,!?]", "Hello.World!How,Are?You");
        assertEquals(5, result5.size());
        assertEquals("Hello", result5.get(0));
        assertEquals("World", result5.get(1));
        assertEquals("How", result5.get(2));
        assertEquals("Are", result5.get(3));
        assertEquals("You", result5.get(4));

        // 6. 无匹配时返回包含原字符串的列表
        List<String> result6 = RegexTool.split("xyz", "hello world");
        assertEquals(1, result6.size());
        assertEquals("hello world", result6.get(0));

        // 7. 空值和空白字符串测试
        assertTrue(RegexTool.split(null, "test").isEmpty());
        assertTrue(RegexTool.split("", "test").isEmpty());
        assertTrue(RegexTool.split("   ", "test").isEmpty());
        assertTrue(RegexTool.split("test", null).isEmpty());
        assertFalse(RegexTool.split("test", "").isEmpty());         // origin data
        assertFalse(RegexTool.split("test", "   ").isEmpty());      // origin data
        assertTrue(RegexTool.split(null, null).isEmpty());

        // 8. 无效正则表达式（应返回空列表）
        assertTrue(RegexTool.split("[invalid", "test").isEmpty());
        assertTrue(RegexTool.split("*", "test").isEmpty());

        // 9. 分割结果包含空字符串
        List<String> result9 = RegexTool.split(",", ",apple,,banana,");
        assertEquals(4, result9.size());
        assertEquals("", result9.get(0));
        assertEquals("apple", result9.get(1));
        assertEquals("", result9.get(2));
        assertEquals("banana", result9.get(3));

        // 10. 分割 - 换行符
        List<String> result10 = RegexTool.split("\\r?\\n", "line1\nline2\r\nline3");
        assertEquals(3, result10.size());
        assertEquals("line1", result10.get(0));
        assertEquals("line2", result10.get(1));
        assertEquals("line3", result10.get(2));

        // 11. 分割 - Tab 字符
        List<String> result11 = RegexTool.split("\\t", "col1\tcol2\tcol3");
        assertEquals(3, result11.size());
        assertEquals("col1", result11.get(0));
        assertEquals("col2", result11.get(1));
        assertEquals("col3", result11.get(2));

        // 12. 分割 - 连续分隔符
        List<String> result12 = RegexTool.split("\\s+", "hello   world    test");
        assertEquals(3, result12.size());
        assertEquals("hello", result12.get(0));
        assertEquals("world", result12.get(1));
        assertEquals("test", result12.get(2));

        // 13. CSV 格式分割（简单场景）
        List<String> result13 = RegexTool.split(",", "name,age,city");
        assertEquals(3, result13.size());
        assertEquals("name", result13.get(0));
        assertEquals("age", result13.get(1));
        assertEquals("city", result13.get(2));

        // 14. 路径分割
        List<String> result14 = RegexTool.split("[/\\\\]", "/usr/local/bin/test");
        assertEquals(5, result14.size());
        assertEquals("", result14.get(0));
        assertEquals("usr", result14.get(1));
        assertEquals("local", result14.get(2));
        assertEquals("bin", result14.get(3));
        assertEquals("test", result14.get(4));
    }


    @Test
    public void test() {
        // 1. 邮箱验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_EMAIL, "test@example.com"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_EMAIL, "user.name@domain.org"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_EMAIL, "invalid-email"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_EMAIL, "@example.com"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_EMAIL, "test@"));

        // 2. 手机号验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_MOBILE, "13812345678"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_MOBILE, "19812345678"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_MOBILE, "12345678901"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_MOBILE, "1381234567"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_MOBILE, "138123456789"));

        // 3. 身份证验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_ID_CARD, "110101199001011234"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_ID_CARD, "11010120000101123X"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_ID_CARD, "123456789012345678"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_ID_CARD, "11010119900101123"));

        // 4. URL验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_URL, "https://www.example.com"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_URL, "http://test.org/path"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_URL, "ftp://files.example.com"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_URL, "not a url"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_URL, "www.example.com"));

        // 5. IPv4地址验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_IP, "192.168.1.1"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_IP, "255.255.255.255"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_IP, "0.0.0.0"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_IP, "256.1.1.1"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_IP, "192.168.1"));

        // 6. 日期格式验证 (yyyy-MM-dd)
        assertTrue(RegexTool.matches(RegexTool.REGEX_DATE, "2024-01-15"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_DATE, "2023-12-31"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_DATE, "2024/01/15"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_DATE, "2024-1-15"));

        // 7. 时间格式验证 (HH:mm:ss)
        assertTrue(RegexTool.matches(RegexTool.REGEX_TIME, "12:30:45"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_TIME, "00:00:00"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_TIME, "23:59:59"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_TIME, "12:30"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_TIME, "24:00:00"));

        // 8. 日期时间验证 (yyyy-MM-dd HH:mm:ss)
        assertTrue(RegexTool.matches(RegexTool.REGEX_DATETIME, "2024-01-15 12:30:45"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_DATETIME, "2023-12-31 23:59:59"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_DATETIME, "2024-01-15"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_DATETIME, "2024/01/15 12:30:45"));

        // 9. 邮政编码验证（中国）
        assertTrue(RegexTool.matches(RegexTool.REGEX_POSTAL_CODE, "100000"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_POSTAL_CODE, "200000"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_POSTAL_CODE, "10000"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_POSTAL_CODE, "1000000"));

        // 10. 中文字符验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_CHINESE, "你好世界"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_CHINESE, "中文"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_CHINESE, "hello"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_CHINESE, "你好world"));

        // 11. 用户名验证（字母数字下划线，4-16位）
        assertTrue(RegexTool.matches(RegexTool.REGEX_USERNAME, "user123"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_USERNAME, "test_user"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_USERNAME, "User"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_USERNAME, "usr"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_USERNAME, "user@123"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_USERNAME, "thisusernameistoolong"));

        // 12. 密码验证（至少8位，包含大小写字母和数字）
        assertTrue(RegexTool.matches(RegexTool.REGEX_PASSWORD, "Password1"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_PASSWORD, "Abcdefg1"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_PASSWORD, "password1"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_PASSWORD, "PASSWORD1"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_PASSWORD, "Password"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_PASSWORD, "Pass1"));

        // 13. 车牌号验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_CAR_NUMBER, "京A12345"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_CAR_NUMBER, "沪B67890"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_CAR_NUMBER, "粤B12345"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_CAR_NUMBER, "AB12345"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_CAR_NUMBER, "京O12345"));

        // 14. 银行卡号验证（13-19位）
        assertTrue(RegexTool.matches(RegexTool.REGEX_BANK_CARD, "6222021234567890123"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_BANK_CARD, "1234567890123456"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_BANK_CARD, "123456789012"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_BANK_CARD, "12345678901234567890"));

        // 15. 统一社会信用代码验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_TAX_NUMBER, "91110000000000000X"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_TAX_NUMBER, "913100000000000000"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_TAX_NUMBER, "12345678901234567"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_TAX_NUMBER, "O123456789012345678"));

        // 16. 固定电话验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_PHONE, "010-12345678"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_PHONE, "021-12345678"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_PHONE, "12345678"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_PHONE, "1234567"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_PHONE, "010-123456789"));

        // 17. 整数验证（正负）
        assertTrue(RegexTool.matches(RegexTool.REGEX_INTEGER, "123"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_INTEGER, "-123"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_INTEGER, "0"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_INTEGER, "12.3"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_INTEGER, "abc"));

        // 18. 小数验证（正负）
        assertTrue(RegexTool.matches(RegexTool.REGEX_DECIMAL, "123.45"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_DECIMAL, "-123.45"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_DECIMAL, "123"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_DECIMAL, "0.5"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_DECIMAL, "abc"));

        // 19. 正整数验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_POSITIVE_INTEGER, "1"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_POSITIVE_INTEGER, "123"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_POSITIVE_INTEGER, "0"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_POSITIVE_INTEGER, "-123"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_POSITIVE_INTEGER, "12.3"));

        // 20. 十六进制颜色验证
        assertTrue(RegexTool.matches(RegexTool.REGEX_HEX_COLOR, "#FF0000"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_HEX_COLOR, "#ff0000"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_HEX_COLOR, "FF0000"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_HEX_COLOR, "#F00"));
        assertTrue(RegexTool.matches(RegexTool.REGEX_HEX_COLOR, "F00"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_HEX_COLOR, "#GGG000"));
        assertFalse(RegexTool.matches(RegexTool.REGEX_HEX_COLOR, "#FFFF"));
    }



}
