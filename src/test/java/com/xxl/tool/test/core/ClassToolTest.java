package com.xxl.tool.test.core;

import com.xxl.tool.core.ClassTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassToolTest {
    private static final Logger logger = LoggerFactory.getLogger(ClassToolTest.class);

    @Test
    public void getClassName() {
        logger.info("{}", ClassTool.getClass(new ClassToolTest()));

        logger.info("{}", ClassTool.getClassName(new ClassToolTest(), false));
        logger.info("{}", ClassTool.getClassName(new ClassToolTest(), true));

        logger.info("{}", ClassTool.getClassName(ClassToolTest.class, false));
        logger.info("{}", ClassTool.getClassName(ClassToolTest.class, true));

    }

    @Test
    public void getPackageName() {
        logger.info("getPackageName = {}", ClassTool.getPackageName(ReflectionToolTest.class));
        logger.info("getPackageName = {}", ClassTool.getPackageName(ReflectionToolTest.class.getName()));
    }

    @Test
    public void isAssignable() {
        logger.info("{}", ClassTool.isAssignable(Number.class, Integer.class));
        logger.info("{}", ClassTool.isAssignable(Integer.class, Integer.class));
    }

    @Test
    public void resolveClass() throws ClassNotFoundException {
        logger.info("{}", ClassTool.resolveClass("int"));
        logger.info("{}", ClassTool.resolveClass("java.lang.Integer"));
        logger.info("{}", ClassTool.resolveClass("com.xxl.tool.test.core.ClassToolTest"));
    }

    @Test
    public void isPrimitive() {
        Assertions.assertTrue(ClassTool.isPrimitive(int.class));
        Assertions.assertTrue(ClassTool.isPrimitiveWrapper(Integer.class));
        Assertions.assertTrue(ClassTool.isPrimitiveOrWrapper(Integer.class));
        Assertions.assertTrue(ClassTool.isPrimitiveOrWrapperOrString(String.class));

        Assertions.assertFalse(ClassTool.isPrimitive(ClassToolTest.class));
        Assertions.assertFalse(ClassTool.isPrimitiveWrapper(ClassToolTest.class));
        Assertions.assertFalse(ClassTool.isPrimitiveOrWrapper(ClassToolTest.class));
        Assertions.assertFalse(ClassTool.isPrimitiveOrWrapperOrString(ClassToolTest.class));
    }

}
