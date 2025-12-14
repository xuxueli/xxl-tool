package com.xxl.tool.test.core;

import com.xxl.tool.core.ReflectionTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionToolTest {
    private static Logger logger = LoggerFactory.getLogger(ReflectionToolTest.class);

    public static final String name = null;
    private String test3 = "111";
    public void test2(){
        System.out.println("test2");
    }

    @Test
    public void getMethod() {
        logger.info("getMethod: {}", ReflectionTool.getMethod(ReflectionToolTest.class, "getMethod"));
        logger.info("getDeclaredMethod: {}", ReflectionTool.getDeclaredMethod(ReflectionToolTest.class, "getMethod"));

        logger.info("getMethods: {}", Arrays.stream(ReflectionTool.getMethods(ReflectionToolTest.class, true)).map(Method::getName).toList());
        logger.info("getMethods: {}", Arrays.stream(ReflectionTool.getMethods(ReflectionToolTest.class, false)).map(Method::getName).toList());
    }

    @Test
    public void getField() {
        logger.info("getFields: {}", Arrays.stream(ReflectionTool.getFields(ReflectionToolTest.class, true)).map(Field::getName).toList());
        logger.info("getFields: {}", Arrays.stream(ReflectionTool.getFields(ReflectionToolTest.class, false)).map(Field::getName).toList());

        logger.info("field1 = {}", ReflectionTool.getField(ReflectionToolTest.class, "name"));
        logger.info("field1 = {}", ReflectionTool.getDeclaredField(ReflectionToolTest.class, "name"));

        Field field1 = ReflectionTool.getField(ReflectionToolTest.class, "name");
        logger.info("isPublicStaticFinal: {}", ReflectionTool.isPublicStaticFinal(field1));

        Field field3 = ReflectionTool.getDeclaredField(ReflectionToolTest.class, "test3");
        logger.info("field3 = {}", field3);
        logger.info("{}", this.test3);

        field3.setAccessible(true);
        ReflectionTool.makeAccessible(field3);
        ReflectionTool.setFieldValue(field3, this, "222");
        logger.info("{}", this.test3);
    }

    @Test
    public void test03() {
        Method method = ReflectionTool.getMethod(ReflectionToolTest.class, "test2");
        logger.info("method = {}", method);
        ReflectionTool.makeAccessible(method);
        ReflectionTool.invokeMethod(method, new ReflectionToolTest());
    }

    @Test
    public void test04() {
        ReflectionTool.doWithFields(ReflectionToolTest.class, new ReflectionTool.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                logger.info("doWith - field = {}", field);
                logger.info("doWith - field = {}", field);
            }
        });
    }

}
