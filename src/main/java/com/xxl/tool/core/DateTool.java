package com.xxl.tool.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日期时间工具（date util）：覆盖 {@link Date} 与字符串互转、{@link LocalDateTime}/{@link LocalDate}/
 * {@link LocalTime}/{@link YearMonth} 的解析与格式化、按日历字段加减与设置日期，以及日期差值计算等能力。
 *
 * <p>约定：
 * <ul>
 *   <li>使用 {@link SimpleDateFormat} 的 {@code parse}/{@code format} 按当前线程默认时区与
 *       {@link Locale#getDefault()} 处理，且解析默认宽松（lenient）：越界字段会顺延、
 *       尾部多余字符会被忽略，如 "2026-02-30" 会解析为 2026-03-02。</li>
 *   <li>{@code java.time} 相关方法使用 {@link DateTimeFormatter#ofPattern(String)} 与默认 {@link Locale}，
 *       解析采用 SMART 策略，如 2 月 30 日会调整为当月最后一天；且要求完整匹配格式，
 *       尾部存在多余字符时抛出 {@link java.time.format.DateTimeParseException}。</li>
 *   <li>基于 {@link Date} 的加减、设置方法均返回新对象，不修改入参；时区换算使用
 *       {@link ZoneId#systemDefault()}。</li>
 * </ul>
 *
 * @author xuxueli 2018-08-19 01:24:11
 */
public class DateTool {
    private static Logger logger = LoggerFactory.getLogger(DateTool.class);

    // ---------------------- pattern  ----------------------

    /**
     * 日期时间格式，如 {@code "2020-12-28 10:00:00"}
     */
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式，如 {@code "2020-12-28"}
     */
    public static final String DATE = "yyyy-MM-dd";

    /**
     * 时间格式（含秒），如 {@code "10:00:00"}
     */
    public static final String TIME = "HH:mm:ss";
    /**
     * 时间格式（不含秒），如 {@code "10:00"}
     */
    public static final String TIME_WITHOUT_SECOND = "HH:mm";


    // ---------------------- parse / format date  ----------------------

    private static final ThreadLocal<ConcurrentHashMap<String, DateFormat>> dateFormatThreadLocal = ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * 获取当前线程缓存的 {@link DateFormat}，同一线程内相同 pattern 复用实例
     *
     * <p>{@link SimpleDateFormat} 非线程安全，因此按线程缓存，避免加锁。
     *
     * @param pattern 日期格式，前后空白会被去除，不能为 {@code null} 或空白
     * @return 当前线程内复用的 {@link DateFormat}
     * @throws IllegalArgumentException pattern 为 {@code null} 或空白时抛出
     */
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
     * 按指定格式将字符串解析为 {@link Date}
     *
     * <p>使用当前线程默认时区与默认 {@link Locale}，解析为宽松模式：越界字段会顺延
     * （如 "2026-02-30" 解析为 2026-03-02），尾部多余字符会被忽略。
     *
     * @param dateString 待解析的日期字符串，不能为 {@code null}
     * @param pattern    日期格式，如 {@link #DATE_TIME}；为 {@code null} 或空白时抛出 {@link IllegalArgumentException}
     * @return 解析得到的 {@link Date}
     * @throws RuntimeException 字符串与格式不匹配导致解析失败时抛出，原始 {@link ParseException} 作为 cause
     */
    public static Date parse(String dateString, String pattern) {
        try {
            return loadDateFormat(pattern).parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("parse error.", e);
        }
    }

    /**
     * 按 {@link #DATE_TIME}（"yyyy-MM-dd HH:mm:ss"）格式解析字符串为 {@link Date}
     *
     * @param dateString 待解析的日期时间字符串，如 "2020-12-28 10:00:00"，不能为 {@code null}
     * @return 解析得到的 {@link Date}
     * @throws RuntimeException 字符串与 {@link #DATE_TIME} 格式不匹配时抛出
     */
    public static Date parseDateTime(String dateString) {
        return parse(dateString, DATE_TIME);
    }

    /**
     * 按 {@link #DATE}（"yyyy-MM-dd"）格式解析字符串为 {@link Date}（时间为 00:00:00）
     *
     * @param dateString 待解析的日期字符串，如 "2020-12-28"，不能为 {@code null}
     * @return 解析得到的 {@link Date}
     * @throws RuntimeException 字符串与 {@link #DATE} 格式不匹配时抛出
     */
    public static Date parseDate(String dateString){
        return parse(dateString, DATE);
    }

    /**
     * 按指定格式将 {@link Date} 格式化为字符串
     *
     * <p>使用当前线程默认时区与默认 {@link Locale}。
     *
     * @param date    待格式化的日期，不能为 {@code null}
     * @param pattern 日期格式，如 {@link #DATE_TIME}；为 {@code null} 或空白时抛出 {@link IllegalArgumentException}
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String pattern) {
        return loadDateFormat(pattern).format(date);
    }

    /**
     * 按 {@link #DATE}（"yyyy-MM-dd"）格式格式化日期
     *
     * @param date 待格式化的日期，不能为 {@code null}
     * @return 格式化后的日期字符串，如 "2020-12-28"
     */
    public static String formatDate(Date date) {
        return format(date, DATE);
    }

    /**
     * 按 {@link #DATE_TIME}（"yyyy-MM-dd HH:mm:ss"）格式格式化日期时间
     *
     * @param date 待格式化的日期时间，不能为 {@code null}
     * @return 格式化后的日期时间字符串，如 "2020-12-28 10:00:00"
     */
    public static String formatDateTime(Date date) {
        return format(date, DATE_TIME);
    }

    // ---------------------- parse / format with DateTimeFormatter ----------------------


    /**
     * 按指定格式解析字符串为 {@link LocalDateTime}
     *
     * <p>基于 {@link DateTimeFormatter#ofPattern(String)}，使用默认 {@link Locale}；
     * 解析采用 SMART 策略，如 2 月 30 日会调整为 2 月最后一天，月份越界则抛出异常。
     *
     * @param dateString 待解析的日期时间字符串，不能为 {@code null}
     * @param pattern    日期时间格式，如 {@link #DATE_TIME}，不能为 {@code null}
     * @return 解析得到的 {@link LocalDateTime}
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static LocalDateTime parseLocalDateTime(String dateString, String pattern) {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 按指定格式解析字符串为 {@link LocalDate}
     *
     * <p>解析采用 SMART 策略，如 2 月 30 日会调整为 2 月最后一天；要求完整匹配格式，
     * 尾部存在多余字符时抛出 {@link java.time.format.DateTimeParseException}。
     *
     * @param dateString 待解析的日期字符串，不能为 {@code null}
     * @param pattern    日期格式，如 {@link #DATE}，不能为 {@code null}
     * @return 解析得到的 {@link LocalDate}
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static LocalDate parseLocalDate(String dateString, String pattern) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 按指定格式解析字符串为 {@link LocalTime}
     *
     * @param dateString 待解析的时间字符串，不能为 {@code null}
     * @param pattern    时间格式，如 {@link #TIME}，不能为 {@code null}
     * @return 解析得到的 {@link LocalTime}
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static LocalTime parseLocalTime(String dateString, String pattern) {
        return LocalTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 按指定格式解析字符串为 {@link YearMonth}
     *
     * @param dateString 待解析的年月字符串，如 "2020-12"，不能为 {@code null}
     * @param pattern    年月格式，如 "yyyy-MM"，不能为 {@code null}
     * @return 解析得到的 {@link YearMonth}
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static YearMonth parseYearMonth(String dateString, String pattern) {
        return YearMonth.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 将 {@link Temporal} 格式化为字符串，支持 {@link LocalDateTime}、{@link LocalDate}、
     * {@link LocalTime}、{@link YearMonth} 四种类型
     *
     * @param temporal 待格式化的时间对象，不能为 {@code null}；仅支持上述四种类型
     * @param pattern  日期格式，如 {@link #DATE_TIME}，不能为 {@code null}
     * @return 格式化后的字符串
     * @throws IllegalArgumentException temporal 不是受支持的类型时抛出
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
     * 增加指定年数，返回新对象，不修改入参
     *
     * <p>闰年 2 月 29 日加 1 年会得到次年 2 月 28 日。
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的年数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addYears(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusYears(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加指定月数，返回新对象，不修改入参
     *
     * <p>月末会自动对齐：如 1 月 31 日加 1 个月得到 2 月 28 日（闰年为 2 月 29 日）。
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的月数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addMonths(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusMonths(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加指定天数，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的天数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addDays(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusDays(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加指定小时数，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的小时数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addHours(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusHours(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加指定分钟数，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的分钟数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addMinutes(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusMinutes(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加指定秒数，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的秒数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addSeconds(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusSeconds(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加指定周数（1 周 = 7 天），返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的周数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addWeeks(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusWeeks(amount);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加指定毫秒数，返回新对象，不修改入参
     *
     * <p>内部将毫秒换算为纳秒（{@code amount * 1000000}），amount 极大时可能发生 long 溢出。
     *
     * @param date   原始日期，不能为 {@code null}
     * @param amount 增加的毫秒数，可为负数表示减少
     * @return 增加后的新 {@link Date}
     */
    public static Date addMilliseconds(final Date date, final long amount) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime newDateTime = dateTime.plusNanos(amount * 1000000);
        return Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    // ---------------------- set by calendar field ----------------------

    /**
     * 将指定日历字段设置为目标值，返回新对象，不修改入参
     *
     * <p>使用系统默认时区与默认 {@link Locale} 创建 {@link Calendar}，并关闭宽松模式
     * （{@link Calendar#setLenient(boolean)} 为 {@code false}）：字段值越界时抛出异常，不做顺延。
     *
     * @param date          原始日期，不能为 {@code null}，不会被修改
     * @param calendarField 日历字段，如 {@link Calendar#YEAR}、{@link Calendar#MONTH}
     * @param amount        字段的目标值
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 字段值越界时抛出
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
     * 设置年份，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}，不会被修改
     * @param amount 目标年份，如 2026
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 年份越界时抛出
     */
    public static Date setYears(final Date date, final int amount) {
        return set(date, Calendar.YEAR, amount);
    }

    /**
     * 设置月份，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}，不会被修改
     * @param amount 目标月份，{@code 0} 表示一月（{@code JANUARY}），{@code 11} 表示十二月（{@code DECEMBER}）
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 月份越界（不在 0-11 之间）时抛出
     */
    public static Date setMonths(final Date date, final int amount) {
        return set(date, Calendar.MONTH, amount);
    }

    /**
     * 设置当月第几天，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}，不会被修改
     * @param amount 目标日，范围为 1 到当月天数
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 日期越界（如 4 月 31 日）时抛出
     */
    public static Date setDays(final Date date, final int amount) {
        return set(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 设置小时（24 小时制），返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}，不会被修改
     * @param amount 目标小时，范围为 0-23
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 小时越界时抛出
     */
    public static Date setHours(final Date date, final int amount) {
        return set(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 设置分钟，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}，不会被修改
     * @param amount 目标分钟，范围为 0-59
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 分钟越界时抛出
     */
    public static Date setMinutes(final Date date, final int amount) {
        return set(date, Calendar.MINUTE, amount);
    }

    /**
     * 设置秒，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}，不会被修改
     * @param amount 目标秒，范围为 0-59
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 秒越界时抛出
     */
    public static Date setSeconds(final Date date, final int amount) {
        return set(date, Calendar.SECOND, amount);
    }

    /**
     * 设置毫秒，返回新对象，不修改入参
     *
     * @param date   原始日期，不能为 {@code null}，不会被修改
     * @param amount 目标毫秒，范围为 0-999
     * @return 设置后的新 {@link Date}
     * @throws IllegalArgumentException 毫秒越界时抛出
     */
    public static Date setMilliseconds(final Date date, final int amount) {
        return set(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 设置为当天起始时刻（"yyyy-MM-dd 00:00:00.000"），返回新对象，不修改入参
     *
     * @param date 原始日期，不能为 {@code null}，不会被修改
     * @return 当天起始时刻的新 {@link Date}，如 "2020-12-28 00:00:00"
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
     * 1 毫秒对应的毫秒数
     */
    public static final long MILLIS_PER_MS = 1;

    /**
     * 1 秒对应的毫秒数
     */
    public static final long MILLIS_PER_SECOND = 1000;

    /**
     * 1 分钟对应的毫秒数
     */
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;

    /**
     * 1 小时对应的毫秒数
     */
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

    /**
     * 1 天对应的毫秒数
     */
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

    /**
     * 1 周对应的毫秒数
     */
    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * 7;

    /**
     * 1 个月（按 30 天计）对应的毫秒数
     */
    public static final long MILLIS_PER_MONTH_30 = MILLIS_PER_DAY * 30;

    /**
     * 1 年（按 365 天计）对应的毫秒数
     */
    public static final long MILLIS_PER_YEAR_365 = MILLIS_PER_DAY * 365;


    /**
     * 计算两个日期之间完整的日历年/月数。
     *
     * <p>仅取日期部分，时间与 DST 不影响结果；结束日未达到起始日对应日时不计入一个单位。
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @param unit      支持 {@link ChronoUnit#YEARS}、{@link ChronoUnit#MONTHS}
     * @return 完整单位数（endDate - beginDate），可为负数
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    private static long betweenCalendar(Date beginDate, Date endDate, ChronoUnit unit) {
        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("date must not be null");
        }

        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate begin = beginDate.toInstant().atZone(zoneId).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(zoneId).toLocalDate();
        return unit.between(begin, end);
    }

    /**
     * 计算两个日期在指定日历字段上的差值（endDate - beginDate）
     *
     * <p>按固定毫秒数换算：年按 365 天、月按 30 天、周按 7 天，不做真实日历对齐，
     * 因此闰年、大小月及夏令时下与自然语义可能不一致。结果为整数除法（向零取整），
     * 不足一个单位的部分被截断。
     *
     * @param beginDate     起始日期，不能为 {@code null}
     * @param endDate       结束日期，不能为 {@code null}
     * @param calendarField 日历字段，如 {@link Calendar#YEAR}
     * @return 差值（endDate - beginDate），可能为负数
     * @throws IllegalArgumentException 任一日期为 {@code null}，或字段不受支持时抛出
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
     * 计算两个日期相差的年数（endDate - beginDate）
     *
     * <p>按系统默认时区取两个日期的 {@link LocalDate}，计算完整的日历年数：结束日未达到起始日的
     * 对应月日时不计入一年，不足一年的部分被截断。例如 {@code 2020-02-29} 到 {@code 2021-02-28}
     * 为 0 年，到 {@code 2021-03-01} 为 1 年。
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @return 相差的完整年数，可为负数
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    public static long betweenYear(Date beginDate, Date endDate) {
        return betweenCalendar(beginDate, endDate, ChronoUnit.YEARS);
    }

    /**
     * 计算两个日期相差的月数（endDate - beginDate）
     *
     * <p>按系统默认时区取两个日期的 {@link LocalDate}，计算完整的日历月数：结束日未达到起始日的
     * 对应日时不计入一个月，不足一个月的部分被截断。例如 {@code 2026-01-15} 到 {@code 2026-03-15}
     * 为 2 个月，{@code 2026-01-31} 到 {@code 2026-02-28} 为 0 个月。
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @return 相差的完整月数，可为负数
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    public static long betweenMonth(Date beginDate, Date endDate) {
        return betweenCalendar(beginDate, endDate, ChronoUnit.MONTHS);
    }

    /**
     * 计算两个日期相差的天数（endDate - beginDate）
     *
     * <p>按 24 小时/天固定换算并截断不足一天的部分，不按自然日对齐。
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @return 相差天数，可为负数
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    public static long betweenDay(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.DAY_OF_MONTH);
    }

    /**
     * 计算两个日期相差的小时数（endDate - beginDate）
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @return 相差小时数，可为负数，不足一小时的部分被截断
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    public static long betweenHour(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.HOUR_OF_DAY);
    }

    /**
     * 计算两个日期相差的分钟数（endDate - beginDate）
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @return 相差分钟数，可为负数，不足一分钟的部分被截断
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    public static long betweenMinute(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.MINUTE);
    }

    /**
     * 计算两个日期相差的秒数（endDate - beginDate）
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @return 相差秒数，可为负数，不足一秒的部分被截断
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    public static long betweenSecond(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.SECOND);
    }

    /**
     * 计算两个日期相差的周数（endDate - beginDate）
     *
     * <p>按 7 天/周固定换算并截断不足一周的部分，非自然周对齐。
     *
     * @param beginDate 起始日期，不能为 {@code null}
     * @param endDate   结束日期，不能为 {@code null}
     * @return 相差周数，可为负数
     * @throws IllegalArgumentException 任一日期为 {@code null} 时抛出
     */
    public static long betweenWeek(Date beginDate, Date endDate) {
        return between(beginDate, endDate, Calendar.WEEK_OF_YEAR);
    }



    // ---------------------- judge ----------------------

    /**
     * 判断两个 {@link Calendar} 是否表示同一天（同一纪元、同一年、年中同一天）
     *
     * <p>按两个 Calendar 各自的时区判断，不修改入参。
     *
     * @param cal1 第一个日历，不能为 {@code null}
     * @param cal2 第二个日历，不能为 {@code null}
     * @return 同一天返回 {@code true}，否则返回 {@code false}
     * @throws NullPointerException 任一参数为 {@code null} 时抛出
     */
    public static boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        Objects.requireNonNull(cal1, "cal1");
        Objects.requireNonNull(cal2, "cal2");
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

}