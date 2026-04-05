package com.xxl.tool.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regex Tool
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
    public static final String REGEX_HEX_COLOR = "^#?([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$";


    /**
     * 验证字符串是否匹配正则表达式
     *
     * <pre>
     *     RegexTool.matches("^[a-zA-Z0-9_]{4,16}$", "username");
     *     RegexTool.matches("^\\d{6}$", "123456");
     * </pre>
     *
     * @param regex     regular expression
     * @param input     input string
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
     * @param regex     regular expression
     * @param input     input string
     * @return all      matched substring
     */
    public static List<String> findAll(String regex, String input) {
        return findMatches(regex, input, 0, false);
    }

    /**
     * 提取匹配的内容
     *
     * @param regex     regular expression
     * @param input     input string
     * @param group     the group to extract
     * @return 提取的内容列表
     */
    public static List<String> extract(String regex, String input, int group) {
        if (group < 0) {
            throw new IllegalArgumentException("Invalid group index: " + group);
        }
        return findMatches(regex, input, group, true);
    }

    /**
     * 通用匹配方法
     *
     * @param regex         regular expression
     * @param input         input string
     * @param group         group to extract (0 for full match)
     * @param validateGroup whether to validate group count
     * @return              matched strings
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
     * @param regex         regular expression
     * @param input         input string
     * @param replacement   the data to be replaced
     * @return              the string with all the matches replaced
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
     * @param regex regular expression
     * @param input input string
     * @return the array of strings that split with the regex
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
