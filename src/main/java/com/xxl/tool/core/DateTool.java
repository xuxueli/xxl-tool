package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

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
     * @param dateString    date String
     * @param pattern       pattern
     * @param withTime      whether parse with time
     * @return Date
     */
    public static Date parse(String dateString, String pattern, boolean withTime) {
        if (withTime) {
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);
        } else {
            LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
            Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);
        }
    }

    /**
     * parse String to Date, with time
     *
     * @param dateString
     * @param pattern
     * @return
     */
    public static Date parse(String dateString, String pattern) {
        return parse(dateString, pattern, true);
    }

    /**
     * parse String to DateTime, like "yyyy-MM-dd HH:mm:s"
     *
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static Date parseDateTime(String dateString) {
        return parse(dateString, DATE_TIME, true);
    }

    /**
     * parse String to Date, like "yyyy-MM-dd"
     *
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String dateString){
        return parse(dateString, DATE, false);
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


    // ---------------------- set ----------------------

    /**
     * Set the specified field to a date,  returning a new object.
     *
     * @param date
     * @param calendarField
     * @param amount
     * @return
     */
    private static Date set(final Date date, final int calendarField, final int amount) {
        // getInstance() returns a new object, so this method is thread safe.
        final Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(date);
        c.set(calendarField, amount);
        return c.getTime();
    }

    /**
     * set year to a date, returning a new object
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date setYears(final Date date, final int amount) {
        return set(date, Calendar.YEAR, amount);
    }

    /**
     * set month to a date, returning a new object
     *
     * @param date      the first month of the year is <code>JANUARY</code>, which is 0;
     * @param amount
     * @return
     */
    public static Date setMonths(final Date date, final int amount) {
        return set(date, Calendar.MONTH, amount);
    }

    /**
     * set day-of-month to a date, returning a new object
     *
     * @param date      the original is unchanged,
     * @param amount    the amount to set
     * @return
     */
    public static Date setDays(final Date date, final int amount) {
        return set(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * set hour-of-day to a date, returning a new object
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date setHours(final Date date, final int amount) {
        return set(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * set minutes to a date, returning a new object
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date setMinutes(final Date date, final int amount) {
        return set(date, Calendar.MINUTE, amount);
    }

    /**
     * set seconds to a date, returning a new object
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date setSeconds(final Date date, final int amount) {
        return set(date, Calendar.SECOND, amount);
    }

    /**
     * set milliseconds to a date, returning a new object
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date setMilliseconds(final Date date, final int amount) {
        return set(date, Calendar.MILLISECOND, amount);
    }


    // ---------------------- judge ----------------------

    /**
     * is same day
     *
     * @param cal1
     * @param cal2
     * @return
     */
    public static boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        Objects.requireNonNull(cal1, "cal1");
        Objects.requireNonNull(cal2, "cal2");
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

}