package com.xxl.tool.test.core;

import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DateToolTest {
    private static Logger logger = LoggerFactory.getLogger(DateToolTest.class);

    @Test
    public void formatTest() {

        String dateTimeStr = DateTool.formatDateTime(new Date());
        logger.info("origin date = " + dateTimeStr);

        Date date = DateTool.parseDateTime(dateTimeStr);
        logger.info("parse result = " + DateTool.formatDateTime(date));
    }

    @Test
    public void addTime(){
        Date now = new Date();
        logger.info("origin = " + DateTool.formatDateTime(now));
        logger.info("addYears = " + DateTool.formatDateTime(DateTool.addYears(now, 1)));
        logger.info("addMonths = " + DateTool.formatDateTime(DateTool.addMonths(now, 1)));
        logger.info("addDays = " + DateTool.formatDateTime(DateTool.addDays(now, 1)));
        logger.info("addHours = " + DateTool.formatDateTime(DateTool.addHours(now, 1)));
        logger.info("addMinutes = " + DateTool.formatDateTime(DateTool.addMinutes(now, 1)));
        logger.info("addSeconds = " + DateTool.formatDateTime(DateTool.addSeconds(now, 1)));
    }

}
