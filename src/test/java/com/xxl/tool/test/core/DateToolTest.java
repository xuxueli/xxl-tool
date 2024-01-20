package com.xxl.tool.test.core;

import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DateToolTest {
    private static Logger logger = LoggerFactory.getLogger(DateToolTest.class);

    @Test
    public void formatDateTimeTest() {
        logger.info(DateTool.formatDateTime(new Date()));
        logger.info(DateTool.formatDateTime(DateTool.addYears(new Date(), 1)));
    }

}
