package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * date util
 *
 * @author xuxueli 2018-08-19 01:24:11
 */
public class DateTool {
    private static Logger logger = LoggerFactory.getLogger(DateTool.class);

    // ---------------------- pattern  ----------------------
    /**
     * Such as "2020-12-28"
     */
    public static final String DATE = "yyyy-MM-dd";
    /**
     * Such as "2020-12-28 10:00:00"
     */
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    /**
     * Such as "10:00:00"
     */
    public static final String TIME = "HHmmss";
    /**
     * Such as "10:00"
     */
    public static final String TIME_WITHOUT_SECOND = "HH:mm";

    /**
     * Such as "2020-12-28 10:00"
     */
    public static final String DATE_TIME_WITHOUT_SECONDS = "yyyy-MM-dd HH:mm";

    // ---------------------- format  ----------------------
    /**
     * format Date to String
     *
     * @param date date
     * @param pattern pattern
     * @return date string
     */
    public static String format(Date date,String pattern){
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * format date；like "yyyy-MM-dd"
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String formatDate(Date date) {
        return format(date, DATE);
    }

    /**
     * format date；like "yyyy-MM-dd HH:mm:s"
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String formatDateTime(Date date) {
        return format(date, DATE_TIME);
    }

    // ---------------------- parse  ----------------------
    /**
     * parse String to Date
     *
     * @param dateString date String
     * @param pattern pattern
     * @return Date
     */
    public static Date parse(String dateString, String pattern) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * parse date string, like "yyyy-MM-dd"
     *
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String dateString){
        return parse(dateString, DATE);
    }

    /**
     * parse datetime string, like "yyyy-MM-dd HH:mm:s"
     *
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static Date parseDateTime(String dateString) {
        return parse(dateString, DATE_TIME);
    }

    // ---------------------- plus ----------------------

    /**
     * add Years
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addYears(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusYears(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * add Months
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addMonths(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusMonths(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * add Days
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addDays(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusDays(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * add Hours
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addHours(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusHours(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * add Minutes
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addMinutes(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusMinutes(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * add Seconds
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addSeconds(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusSeconds(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * add Milliseconds
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addMilliseconds(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusNanos(amount * 1000000);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}