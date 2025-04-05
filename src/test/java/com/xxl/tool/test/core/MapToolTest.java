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
        map.put("k1", 1);
        map.put("k2", 2);
        map.put("k3", 3);

        logger.info(""+ MapTool.isNotEmpty(map));
        logger.info(""+MapTool.getInteger(map, "k1"));
    }

    @Test
    public void newTest() {
        System.out.println("" + MapTool.newHashMap());
        System.out.println("" + MapTool.newHashMap(
                "k1", 1,
                "k2", 2
        ));
        System.out.println("" + MapTool.newHashMap(
                "k1", 1,
                "k2", 2,
                "k3", 3
        ));
        System.out.println("" + MapTool.newHashMap(
                "k1", 1,
                "k2", 2,
                "k3", 3,
                "k4", 4
        ));
        System.out.println("" + MapTool.newHashMap(
                "k1", 1,
                "k2", 2,
                "k3", 3,
                "k4", 4,
                "k5", 5
        ));

        // 匿名内部类
        System.out.println("" + new HashMap<String, Integer>() {{
            put("k1", 1);
            put("k2", 2);
            put("k3", 3);
        }});

    }

}
