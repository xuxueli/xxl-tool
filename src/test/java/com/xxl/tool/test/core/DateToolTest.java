package com.xxl.tool.test.core;

import com.xxl.tool.core.DateTool;
import org.apache.poi.ss.usermodel.DateUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void betweenCalendarYearAndMonth() {
        // 完整月：1-15 到 3-15 为 2 个月
        assertEquals(2, DateTool.betweenMonth(dateOf(2026, 1, 15), dateOf(2026, 3, 15)));
        // 结束日未达到起始日对应日时不计入
        assertEquals(0, DateTool.betweenMonth(dateOf(2026, 1, 31), dateOf(2026, 2, 28)));
        // 负数方向
        assertEquals(-2, DateTool.betweenMonth(dateOf(2026, 3, 15), dateOf(2026, 1, 15)));

        // 完整年：闰日到次年 2-28 未满一年，到 3-1 满一年
        assertEquals(0, DateTool.betweenYear(dateOf(2020, 2, 29), dateOf(2021, 2, 28)));
        assertEquals(1, DateTool.betweenYear(dateOf(2020, 2, 29), dateOf(2021, 3, 1)));
        assertEquals(1, DateTool.betweenYear(dateOf(2020, 1, 15), dateOf(2021, 1, 15)));
        assertEquals(-1, DateTool.betweenYear(dateOf(2021, 1, 15), dateOf(2020, 1, 15)));

        assertThrows(IllegalArgumentException.class, () -> DateTool.betweenYear(null, new Date()));
        assertThrows(IllegalArgumentException.class, () -> DateTool.betweenMonth(new Date(), null));
    }

    private static Date dateOf(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
