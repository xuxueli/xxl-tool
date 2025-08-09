package com.xxl.tool.test.core;

import com.xxl.tool.core.ReflectionTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionToolTest {
    private static Logger logger = LoggerFactory.getLogger(ReflectionToolTest.class);

    public static final String name = null;
    private String test3 = "111";
    public void test2(){
        System.out.println("test2");
    }

    @Test
    public void test() {
        logger.info("getPackageName = {}", ReflectionTool.getPackageName(ReflectionToolTest.class));
        logger.info("getPackageName = {}", ReflectionTool.getPackageName(ReflectionToolTest.class.getName()));

        Field field1 = ReflectionTool.findField(this.getClass(), "name");
        logger.info("field1 = {}", field1);
        logger.info("{}", ReflectionTool.isPublicStaticFinal(field1));

        Field field3 = ReflectionTool.findField(this.getClass(), "test3");
        logger.info("field3 = {}", field3);
        logger.info("{}", this.test3);
        field3.setAccessible(true);
        ReflectionTool.makeAccessible(field3);
        ReflectionTool.setField(field3, this, "222");
        logger.info("{}", this.test3);

        Method method = ReflectionTool.findMethod(ReflectionToolTest.class, "test2");
        logger.info("method = {}", method);
        ReflectionTool.makeAccessible(method);
        ReflectionTool.invokeMethod(method, new ReflectionToolTest());


        ReflectionTool.doWithFields(ReflectionToolTest.class, new ReflectionTool.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                logger.info("doWith - field = {}", field);
            }
        });
    }

}
