package com.xxl.tool.test.core;

import com.xxl.tool.core.MapTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MapToolTest {

    private static Logger logger = LoggerFactory.getLogger(StringToolTest.class);

    @Test
    public void test() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        logger.info(""+ MapTool.isNotEmpty(map));
        logger.info(""+MapTool.getInteger(map, "a"));
    }

}
