package com.xxl.tool.test.core;

import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void operateTest() {
        /**
         *  a: {1,2,3,3,4,5}
         *  b: {3,4,4,5,6,7}
         *
         *  CollectionTool.union(a,b)(并集):              // {1,2,3,3,4,4,5,6,7}  // 元素为key，出现次数最多的留下、加上单边存在的
         *  CollectionTool.intersection(a,b)(交集):       // {3,4,5}              // 元素为key，出现次数最少的留下
         *  CollectionTool.disjunction(a,b)(交集的补集):   // {1,2,3,4,6,7}
         *  CollectionTool.subtract(a,b)(A与B的差):       // {1,2,3}
         *  CollectionTool.subtract(b,a)(B与A的差):       // {4,6,7}
         */

        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);
        a.add(3);
        a.add(4);
        a.add(5);

        ArrayList<Integer> b = new ArrayList<>();
        b.add(3);
        b.add(4);
        b.add(4);
        b.add(5);
        b.add(6);
        b.add(7);

        logger.info(""+CollectionTool.union(a,b));
        logger.info(""+CollectionTool.intersection(a,b));
        logger.info(""+CollectionTool.disjunction(a,b));
        logger.info(""+CollectionTool.subtract(a,b));
        logger.info(""+CollectionTool.subtract(b,a));
    }

    @Test
    public void newTest() {
        logger.info("list = " + CollectionTool.newArrayList());
        logger.info("list = " + CollectionTool.newArrayList(1,2,3));
    }

}
