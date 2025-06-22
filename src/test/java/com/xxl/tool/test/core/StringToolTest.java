package com.xxl.tool.test.core;

import com.xxl.tool.core.StringTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

public class StringToolTest {
    private static Logger logger = LoggerFactory.getLogger(StringToolTest.class);

    @Test
    public void test() {
        logger.info("" + StringTool.isEmpty("  "));
        logger.info("" + StringTool.isBlank("  "));
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
    public void array() {

        logger.info("tokenizeToArray = " + Arrays.toString(StringTool.tokenizeToArray("a,b,c", ",", true, true)));
        logger.info("tokenizeToArray = " + Arrays.toString(StringTool.tokenizeToArray("a,b ,c", ",", true, true)));
        logger.info("tokenizeToArray = " + Arrays.toString(StringTool.tokenizeToArray("a,b ,c, ", ",", false, false)));

        logger.info("toStringArray = " + Arrays.toString(StringTool.toStringArray(Collections.singletonList("a,b,c"))));
        logger.info("toStringArray = " + Arrays.toString(StringTool.toStringArray(Collections.singletonList("a,b ,c"))));
        logger.info("toStringArray = " + Arrays.toString(StringTool.toStringArray(Collections.singletonList("a,b ,c,"))));
        logger.info("toStringArray = " + Arrays.toString(StringTool.toStringArray(Collections.singletonList("a,b ,c, "))));
    }

}
