package com.xxl.tool.test.freemarker;

import com.xxl.tool.freemarker.FtlTool;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class FtlToolTest {
    private static final Logger logger = LoggerFactory.getLogger(FtlToolTest.class);

    @Test
    public void test() throws TemplateException, IOException {
        // init template path
        FtlTool.init("/Users/admin/Downloads/");

        // generate text by template
        String text = FtlTool.processString("test.ftl", new HashMap<>());
        logger.info(text);
    }

}
