package com.xxl.tool.test.freemarker;

import com.xxl.tool.freemarker.FreemarkerTool;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class FreemarkerToolTest {
    private static final Logger logger = LoggerFactory.getLogger(FreemarkerToolTest.class);

    @Test
    public void test() throws TemplateException, IOException {
        // init template path
        FreemarkerTool.init("/Users/admin/Downloads/");

        // generate text by template
        String text = FreemarkerTool.processString("test.ftl", new HashMap<>());
        logger.info(text);
    }

}
