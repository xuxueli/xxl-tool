package com.xxl.tool.test.core;

import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class StringToolTest {
    private static Logger logger = LoggerFactory.getLogger(StringToolTest.class);

    @Test
    public void test() {
        logger.info("" + StringTool.isEmpty("  "));
        logger.info("" + StringTool.isBlank("  "));
    }

    @Test
    public void isNumericTest() {
        logger.info("isNumeric: " + StringTool.isNumeric(null));
        logger.info("isNumeric: " + StringTool.isNumeric(""));
        logger.info("isNumeric: " + StringTool.isNumeric("  "));
        logger.info("isNumeric: " + StringTool.isNumeric("123"));
        logger.info("isNumeric: " + StringTool.isNumeric("12 3"));
        logger.info("isNumeric: " + StringTool.isNumeric("ab2c"));
        logger.info("isNumeric: " + StringTool.isNumeric("12-3"));
        logger.info("isNumeric: " + StringTool.isNumeric("12.3"));
    }

    @Test
    public void underlineToCamelCaseTest() {
        String text = "aaa_bbb";
        logger.info("text = " + text);
        logger.info("result = " + StringTool.underlineToCamelCase(text));
    }

    @Test
    public void camelCaseToUnderlineTest() {
        String text = "StringToolTest";
        logger.info("text = " + text);
        logger.info("lowerCaseFirst = " + StringTool.lowerCaseFirst(text));
    }

    @Test
    public void substring() {
        String text = "123456789";
        logger.info("text = " + text);

        logger.info("substring = " + StringTool.substring(null, 2) );
        logger.info("substring = " + StringTool.substring("", 2) );
        logger.info("substring = " + StringTool.substring("abc", 0) );
        logger.info("substring = " + StringTool.substring("abc", 2) );
        logger.info("substring = " + StringTool.substring("abc", 4) );
        logger.info("substring = " + StringTool.substring("abc", -2) );

        logger.info("substring2 = " + StringTool.substring(null, 1, 2)  );
        logger.info("substring2 = " + StringTool.substring("", 1 ,  2) );
        logger.info("substring2 = " + StringTool.substring("abc", 1, 2) );
        logger.info("substring2 = " + StringTool.substring("abc", -1, 2) );
        logger.info("substring2 = " + StringTool.substring("abc", 1, 0) );
        logger.info("substring2 = " + StringTool.substring("abc", 1, 5) );
        logger.info("substring2 = " +  StringTool.substring("abc", 2, 1) );

    }

    @Test
    public void join() {

        logger.info("split = " + StringTool.split("a,b,c", ","));
        logger.info("split = " + StringTool.split("a, b ,c", ","));
        logger.info("split = " + StringTool.split("a,,c, ", ","));

        logger.info("join = " + StringTool.join(Arrays.asList("a","b","c"), ","));
        logger.info("join = " + StringTool.join(Arrays.asList("a","b"," c "), ","));
        logger.info("join = " + StringTool.join(Arrays.asList("a","b",""), ","));
        logger.info("join = " + StringTool.join(Arrays.asList("a",null,"c"), ","));
    }

    @Test
    public void format() {

        logger.info("format = " + StringTool.format("hello,{0}!", "world"));
        logger.info("format = " + StringTool.format("hello,{0}!", null));
        logger.info("format = " + StringTool.format("hello,{0}!"));
        logger.info("format = " + StringTool.format("hello,{0}!", "world", "world"));

        logger.info("format = " + StringTool.format("Hello {0}, welcome {1}!"));
        logger.info("format = " + StringTool.format("Hello {0}, welcome {1}!",null));
        logger.info("format = " + StringTool.format("Hello {0}, welcome {1}!",null, null));
        logger.info("format = " + StringTool.format("Hello {0}, welcome {1}!", "Alice"));
        logger.info("format = " + StringTool.format("Hello {0}, welcome {1}!", "Alice", "Jack"));
        logger.info("format = " + StringTool.format("Hello {0}, welcome {1}!", "Alice", "Jack", "Lucy"));

        logger.info("format = " + StringTool.format("Hello {0}, you have {1} messages", "Alice", 5));
        logger.info("format = " + StringTool.format("{1} messages for {0}", "Alice", 5));
        logger.info("format = " + StringTool.format("Hello {0}, welcome {0}!", "Alice"));

        logger.info("format = " + StringTool.format("Balance: {0,number}", 1234.56));
        logger.info("format = " + StringTool.format("Price: {0,number,currency}", 1234.56));
        logger.info("format = " + StringTool.format("Success rate: {0,number,percent}", 0.85));
        logger.info("format = " + StringTool.format("Account: {0,number,#,##0.00}", 1234.5));
    }

    @Test
    public void formatWithMap() {
        logger.info("format = " + StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack", "age", 18)));
        logger.info("format = " + StringTool.formatWithMap("{name} is {age} years old", null));
        logger.info("format = " + StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack")));
        logger.info("format = " + StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack", "age", null)));
    }

    @Test
    public void replace() {
        logger.info("replace = " + StringTool.replace("hello jack, how are you", "jack", "lucy"));
        logger.info("replace = " + StringTool.replace("hello jack, how are you, jack", "jack", "lucy"));
        logger.info("replace = " + StringTool.replace("", "jack", "lucy"));
        logger.info("replace = " + StringTool.replace(null, "jack", "lucy"));
        logger.info("replace = " + StringTool.replace("hello jack, how are you", null, "jack"));
        logger.info("replace = " + StringTool.replace("hello jack, how are you", "", "jack"));
        logger.info("replace = " + StringTool.replace("hello jack, how are you", " ", "-"));
        logger.info("replace = " + StringTool.replace("hello jack, how are you", "jack", null));
        logger.info("replace = " + StringTool.replace("hello jack, how are you", "jack", ""));
        logger.info("replace = " + StringTool.replace("hello jack, how are you", "jack", " "));

    }

    @Test
    public void remove() {
        logger.info("removePrefix = " + StringTool.removePrefix("hello,world", "hello"));
        logger.info("removePrefix = " + StringTool.removePrefix("hello,world", "world"));
        logger.info("removePrefix = " + StringTool.removePrefix("hello,world", "hello,world"));
        logger.info("removePrefix = " + StringTool.removePrefix("hello,world", ""));
        logger.info("removePrefix = " + StringTool.removePrefix("hello,world", null));
        logger.info("removePrefix = " + StringTool.removePrefix("", "world"));
        logger.info("removePrefix = " + StringTool.removePrefix(null, "world"));


        logger.info("removeSuffix = " + StringTool.removeSuffix("hello,world", "hello"));
        logger.info("removeSuffix = " + StringTool.removeSuffix("hello,world", "world"));
        logger.info("removeSuffix = " + StringTool.removeSuffix("hello,world", "hello,world"));
        logger.info("removeSuffix = " + StringTool.removeSuffix("hello,world", ""));
        logger.info("removeSuffix = " + StringTool.removeSuffix("hello,world", null));
        logger.info("removeSuffix = " + StringTool.removeSuffix("", "world"));
        logger.info("removeSuffix = " + StringTool.removeSuffix(null, "world"));
    }

    @Test
    public void equals() {
        logger.info("equals = " + StringTool.equals("hello", "hello"));
        logger.info("equals = " + StringTool.equals("hello", "world"));
        logger.info("equals = " + StringTool.equals(null, null));
        logger.info("equals = " + StringTool.equals(null, "world"));
        logger.info("equals = " + StringTool.equals("hello", null));

    }

}
