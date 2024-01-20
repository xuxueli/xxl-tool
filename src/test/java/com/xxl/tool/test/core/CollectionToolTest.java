package com.xxl.tool.test.core;

import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class CollectionToolTest {
    private static Logger logger = LoggerFactory.getLogger(CollectionToolTest.class);

    @Test
    public void isEmptyTest() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        logger.info(""+CollectionTool.isEmpty(list));
        logger.info(""+CollectionTool.isNotEmpty(list));
    }

    @Test
    public void containsTest() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        logger.info(""+CollectionTool.contains(list, 3));
        logger.info(""+CollectionTool.contains(list, 4));
    }

}
