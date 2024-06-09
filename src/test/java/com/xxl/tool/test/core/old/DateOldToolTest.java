//package com.xxl.tool.test.core.old;
//
//import com.xxl.tool.core.old.DateOldTool;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Date;
//
//public class DateOldToolTest {
//    private static Logger logger = LoggerFactory.getLogger(DateOldToolTest.class);
//
//    @Test
//    public void formatTest() {
//
//        String dateTimeStr = DateOldTool.formatDateTime(new Date());
//        logger.info("origin date = " + dateTimeStr);
//
//        Date date = DateOldTool.parseDateTime(dateTimeStr);
//        logger.info("parse result = " + DateOldTool.formatDateTime(date));
//    }
//
//    @Test
//    public void addTime(){
//        Date now = new Date();
//        logger.info("origin = " + DateOldTool.formatDateTime(now));
//        logger.info("addYears = " + DateOldTool.formatDateTime(DateOldTool.addYears(now, 1)));
//        logger.info("addMonths = " + DateOldTool.formatDateTime(DateOldTool.addMonths(now, 1)));
//        logger.info("addDays = " + DateOldTool.formatDateTime(DateOldTool.addDays(now, 1)));
//        logger.info("addHours = " + DateOldTool.formatDateTime(DateOldTool.addHours(now, 1)));
//        logger.info("addMinutes = " + DateOldTool.formatDateTime(DateOldTool.addMinutes(now, 1)));
//        logger.info("addSeconds = " + DateOldTool.formatDateTime(DateOldTool.addSeconds(now, 1)));
//    }
//
//}
