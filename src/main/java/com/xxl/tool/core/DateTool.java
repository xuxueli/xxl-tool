package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * date util
 *
 * @author xuxueli 2018-08-19 01:24:11
 */
public class DateTool {
    private static Logger logger = LoggerFactory.getLogger(DateTool.class);

    // ---------------------- pattern  ----------------------

    /**
     * Such as "2020-12-28 10:00:00"
     */
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * Such as "2020-12-28"
     */
    public static final String DATE = "yyyy-MM-dd";

    /**
     * Such as "10:00:00"
     */
    public static final String TIME = "HH:mm:ss";
    /**
     * Such as "10:00"
     */
    public static final String TIME_WITHOUT_SECOND = "HH:mm";


    // ---------------------- parse / format date  ----------------------

    private static final ThreadLocal<ConcurrentHashMap<String, DateFormat>> dateFormatThreadLocal = ThreadLocal.withInitial(ConcurrentHashMap::new);
    private static DateFormat loadDateFormat(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("pattern cannot be empty.");
        }

        return dateFormatThreadLocal.get().computeIfAbsent(
                pattern.trim(), (String k) -> {
                    return new SimpleDateFormat(pattern.trim(), Locale.getDefault());
                }
        );
    }

    /**
     * parse String to Date, with time
     *
     * @param       dateString
     * @param       pattern
     * @return      date
     */
    public static Date parse(String dateString, String pattern) {
        try {
            return loadDateFormat(pattern).parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("parse error.", e);
        }
    }

    /**
     * parse String to DateTime, like "yyyy-MM-dd HH:mm:s"
     *
     * @param   dateString
     * @return
     */
    public static Date parseDateTime(String dateString) {
        return parse(dateString, DATE_TIME);
    }

    /**
     * parse String to Date, like "yyyy-MM-dd"
     *
     * @param dateString
     * @return
     */
    public static Date parseDate(String dateString){
        return parse(dateString, DATE);
    }

    /**
     * format Date to String
     *
     * @param date      date
     * @param pattern   pattern
     * @return          date string
     */
    public static String format(Date date, String pattern) {
        return loadDateFormat(pattern).format(date);
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

    // ---------------------- parse / format with DateTimeFormatter ----------------------


    /**
     * parse String to LocalDateTime
     *
     * @param dateString
     * @param pattern
     * @return
     */
    public static LocalDateTime parseLocalDateTime(String dateString, String pattern) {
        /*LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);*/

        return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * parse String to LocalDate
     *
     * @param dateString
     * @param pattern
     * @return
     */
    public static LocalDate parseLocalDate(String dateString, String pattern) {
        /*LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);*/

        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * parse String to LocalTime
     *
     * @param dateString
     * @param pattern
     * @return
     */
    public static LocalTime parseLocalTime(String dateString, String pattern) {
        /*LocalTime localTime = LocalTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        Instant instant = localTime.atDate(LocalDate.of(2020, 1, 1)).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);*/

        return LocalTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * parse String to YearMonth
     *
     * @param dateString
     * @param pattern
     * @return
     */
    public static YearMonth parseYearMonth(String dateString, String pattern) {
        /*YearMonth localYearMonth = YearMonth.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        Instant instant = localYearMonth.atDay(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);*/

        return YearMonth.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * format temporal to String
     *
     * @param temporal
     * @param pattern
     * @return
     */
    public static String formatTemporal(Temporal temporal, String pattern) {

        if (temporal instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) temporal;
            return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
        } else if (temporal instanceof LocalDate) {
            LocalDate localDate = (LocalDate) temporal;
            return localDate.format(DateTimeFormatter.ofPattern(pattern));
        } else if (temporal instanceof LocalTime) {
            LocalTime localTime = (LocalTime) temporal;
            return localTime.format(DateTimeFormatter.ofPattern(pattern));
        } else if (temporal instanceof YearMonth) {
            YearMonth yearMonth = (YearMonth) temporal;
            return yearMonth.format(DateTimeFormatter.ofPattern(pattern));
        } else {
            throw new IllegalArgumentException("Unsupported temporal type: " + temporal.getClass().getName());
        }

    }


    // ---------------------- plus by calendar field ----------------------

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
     * add Weeks
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addWeeks(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusWeeks(amount);
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


    // ---------------------- set by calendar field ----------------------

    /**
     * Set the specified field to a date,  returning a new object.
     *
     * @param date              the original is unchanged,
     * @param calendarField     like "Calendar.YEAR"
     * @param amount            the amount to set
     * @return
     */
    public static Date set(final Date date, final int calendarField, final int amount) {
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
     * @param date      the original is unchanged,
     * @param amount    the amount to set
     * @return
     */
    public static Date setYears(final Date date, final int amount) {
        return set(date, Calendar.YEAR, amount);
    }

    /**
     * set month to a date, returning a new object
     *
     * @param date      the first month of the year is <code>JANUARY</code>, which is 0;
     * @param amount    the amount to set
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
     * @param date      the original is unchanged,
     * @param amount    the amount to set
     * @return
     */
    public static Date setHours(final Date date, final int amount) {
        return set(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * set minutes to a date, returning a new object
     *
     * @param date      the original is unchanged,
     * @param amount    the amount to set
     * @return
     */
    public static Date setMinutes(final Date date, final int amount) {
        return set(date, Calendar.MINUTE, amount);
    }

    /**
     * set seconds to a date, returning a new object
     *
     * @param date      the original is unchanged,
     * @param amount    the amount to set
     * @return
     */
    public static Date setSeconds(final Date date, final int amount) {
        return set(date, Calendar.SECOND, amount);
    }

    /**
     * set milliseconds to a date, returning a new object
     *
     * @param date      the original is unchanged,
     * @param amount    the amount to set
     * @return
     */
    public static Date setMilliseconds(final Date date, final int amount) {
        return set(date, Calendar.MILLISECOND, amount);
    }

    /**
     * set start of day to a date, like "yyyy-MM-dd 00:00:00"
     *
     * @param date      the original is unchanged,
     * @return          return the start of day, like "yyyy-MM-dd 00:00:00"
     */
    public static Date setStartOfDay(final Date date) {
        // getInstance() returns a new object, so this method is thread safe.
        final Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTime();
    }

    // ---------------------- between ----------------------

    /**
     * one millisecond
     */
    public static final long MILLIS_PER_MS = 1;

    /**
     * millisecond per second
     */
    public static final long MILLIS_PER_SECOND = 1000;

    /**
     * millisecond per minute
     */
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;

    /**
     * millisecond per minute
     */
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

    /**
     * millisecond per hour
     */
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

    /**
     * millisecond per week
     */
    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * 7;

    /**
     * millisecond per month (30 days)
     */
    public static final long MILLIS_PER_MONTH_30 = MILLIS_PER_DAY * 30;

    /**
     * millisecond per year (365 days)
     */
    public static final long MILLIS_PER_YEAR_365 = MILLIS_PER_DAY * 365;


    /**
     * calculate the difference between two dates (endDate - beginDate)
     *
     * @param beginDate         begin date
     * @param endDate           end date
     * @param calendarField     like "Calendar.YEAR"
     * @return                  the difference (endDate - beginDate) between two dates
     */
    private static long between(Date beginDate, Date endDate, int calendarField) {
        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("date must not be null");
        }

        long unitMillis;
        switch (calendarField) {
            case Calendar.YEAR:
                unitMillis = MILLIS_PER_YEAR_365;
                break;
            case Calendar.MONTH:
                unitMillis = MILLIS_PER_MONTH_30;
                break;
            case Calendar.DAY_OF_MONTH:
            case Calendar.DAY_OF_WEEK:
            case Calendar.DAY_OF_YEAR:
                unitMillis = MILLIS_PER_DAY;
                break;
            case Calendar.HOUR:
            case Calendar.HOUR_OF_DAY:
                unitMillis = MILLIS_PER_HOUR;
                break;
            case Calendar.MINUTE:
                unitMillis = MILLIS_PER_MINUTE;
                break;
            case Calendar.SECOND:
                unitMillis = MILLIS_PER_SECOND;
                break;
            case Calendar.MILLISECOND:
                unitMillis = MILLIS_PER_MS;
                break;
            case Calendar.WEEK_OF_YEAR:
            case Calendar.WEEK_OF_MONTH:
                unitMillis = MILLIS_PER_WEEK;
                break;
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + calendarField);
        }

        // calculate
        long diffMillis = endDate.getTime() - beginDate.getTime();
        return diffMillis / unitMillis;
    }

    /**
     * between year
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long betweenYear(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.YEAR);
    }

    /**
     * between month
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long betweenMonth(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.MONTH);
    }

    /**
     * between day
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long betweenDay(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.DAY_OF_MONTH);
    }

    /**
     * between hour
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long betweenHour(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.HOUR_OF_DAY);
    }

    /**
     * between minute
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long betweenMinute(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.MINUTE);
    }

    /**
     * between second
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long betweenSecond(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.SECOND);
    }

    /**
     * between week
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long betweenWeek(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.WEEK_OF_YEAR);
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