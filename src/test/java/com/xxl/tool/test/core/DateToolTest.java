package com.xxl.tool.test.core;

import com.xxl.tool.core.DateTool;
import org.apache.poi.ss.usermodel.DateUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

public class DateToolTest {
    private static Logger logger = LoggerFactory.getLogger(DateToolTest.class);

    @Test
    public void formatTest() {

        // format
        String dateTimeStr = DateTool.formatDateTime(new Date());                   // "yyyy-MM-dd HH:mm:ss"
        logger.info("formatDateTime = " + dateTimeStr);

        String dateStr = DateTool.formatDate(new Date());                           // "yyyy-MM-dd"
        logger.info("formatDate = " + dateStr);

        String formatStr1 = DateTool.format(new Date(), "yyyy-MM-dd HH");
        logger.info("format = " + formatStr1);
        String formatStr2 = DateTool.format(new Date(), "yyyy-MM");
        logger.info("format = " + formatStr2);

        // format
        logger.info("parseDateTime = " + DateTool.formatDateTime( DateTool.parseDateTime(dateTimeStr) ));
        logger.info("parseDate = " + DateTool.formatDateTime( DateTool.parseDate(dateStr) ));
        logger.info("parse1 = " + DateTool.formatDateTime( DateTool.parse(formatStr1, "yyyy-MM-dd HH") ));
        logger.info("parse2 = " + DateTool.formatDateTime( DateTool.parse(formatStr2, "yyyy-MM") ));
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

    @Test
    public void set(){
        logger.info("setYears = " + DateTool.formatDateTime(DateTool.setYears(new Date(), 2025)));
        logger.info("setMonths = " + DateTool.formatDateTime(DateTool.setMonths(new Date(), 0)));
        logger.info("setDays = " + DateTool.formatDateTime(DateTool.setDays(new Date(), 1)));
        logger.info("setHours = " + DateTool.formatDateTime(DateTool.setHours(new Date(), 1)));
        logger.info("setMinutes = " + DateTool.formatDateTime(DateTool.setMinutes(new Date(), 1)));
        logger.info("setSeconds = " + DateTool.formatDateTime(DateTool.setSeconds(new Date(), 1)));
        logger.info("setMilliseconds = " + DateTool.formatDateTime(DateTool.setMilliseconds(new Date(), 1)));

        logger.info("setStartOfDay = " + DateTool.formatDateTime(DateTool.setStartOfDay(new Date())));
    }

    @Test
    public void between(){
        logger.info("between 1year = " + DateTool.betweenYear(DateTool.addYears(new Date(), -1), new Date()));
        logger.info("between 1year = " + DateTool.betweenMonth(DateTool.addYears(new Date(), -1), new Date()));
        logger.info("between 1year = " + DateTool.betweenDay(DateTool.addYears(new Date(), -1), new Date()));
        logger.info("between 1h = " + DateTool.betweenHour(DateTool.addHours(new Date(), -1), new Date()));
        logger.info("between 1h = " + DateTool.betweenMinute(DateTool.addHours(new Date(), -1), new Date()));
        logger.info("between 1h = " + DateTool.betweenSecond(DateTool.addHours(new Date(), -1), new Date()));
        logger.info("between 2w = " + DateTool.betweenWeek(DateTool.addWeeks(new Date(), -2), new Date()));
    }

}
