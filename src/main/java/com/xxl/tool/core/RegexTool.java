package com.xxl.tool.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regex Tool / 正则工具：提供匹配校验、查找、分组提取、替换与分割能力，正则语法基于 {@link Pattern}。
 *
 * <p>所有方法均为静态方法，不修改入参；类无状态，线程安全。查询类方法在入参非法或无匹配时
 * 返回空集合而不会返回 {@code null}，也不会抛出正则语法异常。
 *
 * @author xuxueli 2026-04-05
 */
public class RegexTool {

    /**
     * 常用正则表达式
     */
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";                                            // 1、邮箱
    public static final String REGEX_MOBILE = "^1[3-9]\\d{9}$";                                                                                 // 2、手机号（简化版，兼容所有运营商）
    public static final String REGEX_ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";     // 3、身份证
    public static final String REGEX_URL = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";                                                              // 4、URL
    public static final String REGEX_IP = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";                         // 5、IPv4地址
    public static final String REGEX_DATE = "^\\d{4}-\\d{2}-\\d{2}$";                                                                           // 6、日期格式 (yyyy-MM-dd)
    public static final String REGEX_TIME = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$";                                                             // 7、时间格式 (HH:mm:ss)
    public static final String REGEX_DATETIME = "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}$";                                               // 8、日期时间 (yyyy-MM-dd HH:mm:ss)
    public static final String REGEX_POSTAL_CODE = "^\\d{6}$";                                                                                  // 9、邮政编码（中国）
    public static final String REGEX_CHINESE = "^[\\u4e00-\\u9fa5]+$";                                                                          // 10、中文字符
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9_]{4,16}$";                                                                         // 11、用户名（字母数字下划线，4-16位）
    public static final String REGEX_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";                                             // 12、密码（至少8位，包含大小写字母和数字）
    public static final String REGEX_CAR_NUMBER = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$";     // 13. 车牌号
    public static final String REGEX_BANK_CARD = "^([1-9]\\d{15,18}|[1-9]\\d{12})$";                                                            // 14、银行卡号（13-19位）
    public static final String REGEX_TAX_NUMBER = "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$";                                      // 15、统一社会信用代码
    public static final String REGEX_PHONE = "^(0\\d{2,3}-\\d{7,8}|0\\d{2,3}\\d{7,8}|\\d{8})$";                                                 // 16、固定电话
    public static final String REGEX_INTEGER = "^-?\\d+$";                                                                                      // 17、整数（正负）
    public static final String REGEX_DECIMAL = "^-?\\d+(\\.\\d+)?$";                                                                            // 18、小数（正负）
    public static final String REGEX_POSITIVE_INTEGER = "^[1-9]\\d*$";                                                                          // 19、正整数
    public static final String REGEX_HEX_COLOR = "^#?([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$";                                                          // 20、十六进制颜色


    /**
     * 验证字符串是否匹配正则表达式
     *
     * <p>采用全匹配语义（{@link Matcher#matches()}），要求整个输入串匹配正则，而非仅包含匹配子串。
     * 正则或输入为 {@code null}、正则为空白串、或正则语法非法时均返回 {@code false}。
     *
     * <pre>
     *     RegexTool.matches("^[a-zA-Z0-9_]{4,16}$", "username");
     *     RegexTool.matches("^\\d{6}$", "123456");
     * </pre>
     *
     * @param regex     regular expression，可为 {@code null} 或空白串
     * @param input     input string，可为 {@code null}
     * @return          true if matches, false otherwise
     */
    public static boolean matches(String regex, String input) {
        if (StringTool.isBlank(regex) || input == null) {
            return false;
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            return matcher.matches();
            //return Pattern.matches(regex, input);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * 查找所有匹配正则表达式的子串
     *
     * <p>采用查找语义（{@link Matcher#find()}），返回输入串中所有可匹配的子串（{@code group(0)}）。
     * 正则或输入为 {@code null}、正则为空白串、正则语法非法或无匹配时返回空列表。
     *
     * @param regex     正则表达式，可为 {@code null} 或空白串
     * @param input     输入字符串，可为 {@code null}
     * @return 所有匹配的子串，无匹配时返回空列表，不会返回 {@code null}
     */
    public static List<String> findAll(String regex, String input) {
        return findMatches(regex, input, 0, false);
    }

    /**
     * 提取匹配的内容
     *
     * <p>按 {@link Matcher#find()} 逐个匹配，并取每个匹配中指定分组的内容；{@code group} 为 {@code 0}
     * 表示整个匹配，{@code 1} 起为捕获分组。分组越界或该分组未匹配到内容时跳过该项。
     * 正则或输入为 {@code null}、正则为空白串、正则语法非法或无匹配时返回空列表。
     *
     * @param regex     正则表达式，可为 {@code null} 或空白串
     * @param input     输入字符串，可为 {@code null}
     * @param group     要提取的分组索引，{@code 0} 表示整个匹配
     * @return 提取的内容列表，无匹配时返回空列表，不会返回 {@code null}
     * @throws IllegalArgumentException 当 {@code group} 小于 {@code 0} 时抛出
     */
    public static List<String> extract(String regex, String input, int group) {
        if (group < 0) {
            throw new IllegalArgumentException("Invalid group index: " + group);
        }
        return findMatches(regex, input, group, true);
    }

    /**
     * 通用匹配方法：按查找语义收集匹配结果。
     *
     * @param regex         正则表达式
     * @param input         输入字符串
     * @param group         要提取的分组索引，{@code 0} 表示整个匹配
     * @param validateGroup 是否校验分组索引是否越界
     * @return 匹配结果列表，无匹配时返回空列表
     */
    private static List<String> findMatches(String regex, String input, int group, boolean validateGroup) {
        List<String> result = new ArrayList<>();
        if (StringTool.isBlank(regex) || input == null || group < 0) {
            return result;
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);

            while (matcher.find()) {
                if (validateGroup && group > matcher.groupCount()) {
                    continue;   // skip invalid group
                }
                try {
                    String groupValue = matcher.group(group);
                    if (groupValue != null) {
                        result.add(groupValue);
                    }
                } catch (IndexOutOfBoundsException e) {
                    continue;   // skip if group is out of bounds
                }
            }
        } catch (PatternSyntaxException e) {
            // Return empty list for invalid regex
        }
        return result;
    }

    /**
     * 替换所有匹配正则表达式的子串
     *
     * <p>替换语义与 {@link Matcher#replaceAll(String)} 一致，{@code replacement} 中的 {@code $} 与 {@code \}
     * 具有特殊含义。正则或输入为 {@code null}、正则为空白串、{@code replacement} 为 {@code null}、
     * 或正则语法非法时，原样返回 {@code input}。
     *
     * @param regex         regular expression，可为 {@code null} 或空白串
     * @param input         input string，可为 {@code null}
     * @param replacement   the data to be replaced，可为 {@code null}
     * @return              the string with all the matches replaced；入参非法时返回原输入
     */
    public static String replaceAll(String regex, String input, String replacement) {
        if (StringTool.isBlank(regex) || input == null || replacement == null) {
            return input;
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            return matcher.replaceAll(replacement);
        } catch (PatternSyntaxException e) {
            return input;
        }
    }

    /**
     * 按照正则表达式分割字符串
     *
     * <p>等价于 {@link String#split(String)}，末尾的空字符串会被丢弃。正则或输入为 {@code null}、
     * 正则为空白串、或正则语法非法时返回空列表。
     *
     * @param regex regular expression，可为 {@code null} 或空白串
     * @param input input string，可为 {@code null}
     * @return 分割后的字符串列表；入参非法时返回空列表，不会返回 {@code null}
     */
    public static List<String> split(String regex, String input) {
        if (StringTool.isBlank(regex) || input == null) {
            return new ArrayList<>();
        }
        try {
            String[] splitArray = input.split(regex);
            return Arrays.asList(splitArray);
        } catch (PatternSyntaxException e) {
            return new ArrayList<>();
        }
    }

}
