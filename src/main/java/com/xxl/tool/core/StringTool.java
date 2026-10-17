package com.xxl.tool.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 字符串工具
 *
 * <p>提供空值与空白判断、首尾裁剪、数字校验、子串截取、分割与拼接、格式化、
 * 文本替换、前后缀移除以及空安全比较等常用能力。</p>
 *
 * <p>所有方法均为静态方法，除返回值外不修改调用方传入的对象。</p>
 *
 * @author xuxueli 2020-04-16
 * (some references to other libraries)
 */
public class StringTool {

    /**
     * The empty String {@code ""}.
     */
    public static final String EMPTY = "";
    /**
     * 表示索引查找未命中。
     */
    public static final int INDEX_NOT_FOUND = -1;


    // ---------------------- empty ----------------------

    /**
     * 是否为空字符串
     *
     * <p>{@code null} 与 {@code ""} 返回 {@code true}；仅含空白字符的字符串不算空串。
     * 若需同时判断空白串，请使用 {@link #isBlank(String)}。</p>
     *
     * <pre>
     * StringTool.isEmpty(null)      = true
     * StringTool.isEmpty("")        = true
     * StringTool.isEmpty(" ")       = false
     * StringTool.isEmpty("bob")     = false
     * StringTool.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param str 待校验字符串，可为 {@code null}
     * @return 为 {@code null} 或长度为 0 时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 是否不为空字符串
     *
     * <p>{@link #isEmpty(String)} 的取反；空白字符串视为非空。</p>
     *
     * <pre>
     * StringTool.isNotEmpty(null)      = false
     * StringTool.isNotEmpty("")        = false
     * StringTool.isNotEmpty(" ")       = true
     * StringTool.isNotEmpty("bob")     = true
     * StringTool.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str 待校验字符串，可为 {@code null}
     * @return 非 {@code null} 且长度大于 0 时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isNotEmpty(String str) {
        return !StringTool.isEmpty(str);
    }


    // ---------------------- blank ----------------------

    /**
     * 是否为空白字符串
     *
     * <p>{@code null}、{@code ""} 以及仅由空白字符（{@link String#trim()} 可去除的字符）组成的字符串
     * 均返回 {@code true}。</p>
     *
     * <pre>
     * StringTool.isBlank(null)      = true
     * StringTool.isBlank("")        = true
     * StringTool.isBlank(" ")       = true
     * StringTool.isBlank("bob")     = false
     * StringTool.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str 待校验字符串，可为 {@code null}
     * @return 为 {@code null}，或去除首尾空白后为空时返回 {@code true}
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 是否不为空白字符串
     *
     * <p>{@link #isBlank(String)} 的取反。</p>
     *
     * <pre>
     * StringTool.isNotBlank(null)      = false
     * StringTool.isNotBlank("")        = false
     * StringTool.isNotBlank(" ")       = false
     * StringTool.isNotBlank("bob")     = true
     * StringTool.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str 待校验字符串，可为 {@code null}
     * @return 去除首尾空白后长度大于 0 时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isNotBlank(String str) {
        return !StringTool.isBlank(str);
    }


    // ---------------------- trim ----------------------

    /**
     * 去除首尾空白
     *
     * <p>空值与 {@code null} 保持原样，不抛异常。</p>
     *
     * <pre>
     * StringTool.trim(null)          = null
     * StringTool.trim("")            = ""
     * StringTool.trim("     ")       = ""
     * StringTool.trim("abc")         = "abc"
     * StringTool.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str 待处理字符串，可为 {@code null}
     * @return 去除首尾空白后的字符串；入参为 {@code null} 时返回 {@code null}
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除首尾空白，结果为空时返回 {@code null}
     *
     * <pre>
     * StringTool.trimToNull(null)          = null
     * StringTool.trimToNull("")            = null
     * StringTool.trimToNull("     ")       = null
     * StringTool.trimToNull("abc")         = "abc"
     * StringTool.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str 待处理字符串，可为 {@code null}
     * @return 去除首尾空白后的字符串；入参为 {@code null} 或结果为 {@code ""} 时返回 {@code null}
     */
    public static String trimToNull(String str) {
        String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    /**
     * 去除首尾空白，结果为 {@code null} 时返回空串
     *
     * <pre>
     * StringTool.trimToEmpty(null)          = ""
     * StringTool.trimToEmpty("")            = ""
     * StringTool.trimToEmpty("     ")       = ""
     * StringTool.trimToEmpty("abc")         = "abc"
     * StringTool.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str 待处理字符串，可为 {@code null}
     * @return 去除首尾空白后的字符串；入参为 {@code null} 时返回 {@code ""}
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }


    // ---------------------- isNumeric ----------------------

    /**
     * 是否仅由数字字符组成
     *
     * <p>逐字符使用 {@link Character#isDigit(char)} 判断，因此接受 Unicode 数字；
     * 小数点、正负号、空格均不属于数字字符。</p>
     *
     * <p>{@code null}、空串与仅含空白的字符串均返回 {@code false}。</p>
     *
     * <pre>
     * StringTool.isNumeric(null)   = false
     * StringTool.isNumeric("")     = false
     * StringTool.isNumeric("  ")   = false
     * StringTool.isNumeric("123")  = true
     * StringTool.isNumeric("12 3") = false
     * StringTool.isNumeric("ab2c") = false
     * StringTool.isNumeric("12-3") = false
     * StringTool.isNumeric("12.3") = false
     * </pre>
     *
     * @param str 待校验字符串，可为 {@code null}
     * @return 非空且每个字符均为数字时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isNumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    // ---------------------- Count matches ----------------------

    /**
     * 统计子串出现次数
     *
     * <p>按非重叠方式统计，例如 {@code countMatches("aaa", "aa")} 返回 {@code 1}。
     * 任一参数为 {@code null} 或空串时返回 {@code 0}。</p>
     *
     * <pre>
     * StringTool.countMatches(null, *)       = 0
     * StringTool.countMatches("", *)         = 0
     * StringTool.countMatches("abba", null)  = 0
     * StringTool.countMatches("abba", "")    = 0
     * StringTool.countMatches("abba", "a")   = 2
     * StringTool.countMatches("abba", "ab")  = 1
     * StringTool.countMatches("abba", "xxx") = 0
     * </pre>
     *
     * @param str 待搜索的字符串，可为 {@code null}
     * @param sub 需要统计的子串，可为 {@code null}
     * @return 子串出现的次数；任一参数为 {@code null} 或空串时返回 {@code 0}
     */
    public static int countMatches(String str, String sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != INDEX_NOT_FOUND) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    // ---------------------- upperCase/ lowerCase ----------------------

    /**
     * 首字母转大写，其余字符保持不变
     *
     * <p>使用默认 Locale 转换，结果受 Locale 影响；{@code null} 与空白字符串原样返回。</p>
     *
     * <pre>
     *      StringTool.upperCaseFirst(null)     = null
     *      StringTool.upperCaseFirst("")       = ""
     *      StringTool.upperCaseFirst("abc")    = "Abc"
     * </pre>
     *
     * @param text 待转换字符串，可为 {@code null}
     * @return 首字符大写后的字符串；入参为 {@code null} 或空白时原样返回
     */
    public static String upperCaseFirst(String text) {
        if (isBlank(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * 首字母转小写，其余字符保持不变
     *
     * <p>使用默认 Locale 转换，结果受 Locale 影响；{@code null} 与空白字符串原样返回。</p>
     *
     * <pre>
     *      StringTool.lowerCaseFirst(null)     = null
     *      StringTool.lowerCaseFirst("")       = ""
     *      StringTool.lowerCaseFirst("ABC")    = "aBC"
     * </pre>
     *
     * @param text 待转换字符串，可为 {@code null}
     * @return 首字符小写后的字符串；入参为 {@code null} 或空白时原样返回
     */
    public static String lowerCaseFirst(String text) {
        if (isBlank(text)) {
            return text;
        }
        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    /**
     * 下划线命名转驼峰命名
     *
     * <p>删除下划线，并将其后的首个字符转为大写；连续下划线仅产生一次转换效果，
     * 末尾下划线会直接丢弃。除下划线外不改动其他字符的大小写。</p>
     *
     * <pre>
     *      StringTool.underlineToCamelCase(null)        = null
     *      StringTool.underlineToCamelCase("")          = ""
     *      StringTool.underlineToCamelCase("aaa_bbb")   = "aaaBbb"
     *      StringTool.underlineToCamelCase("_aaa")      = "Aaa"
     * </pre>
     *
     * @param underscoreText 下划线风格的字符串，可为 {@code null}
     * @return 驼峰风格的字符串；入参为 {@code null} 或空白时原样返回
     */
    public static String underlineToCamelCase(String underscoreText) {
        if (isBlank(underscoreText)) {
            return underscoreText;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (int i = 0; i < underscoreText.length(); i++) {
            char ch = underscoreText.charAt(i);
            if ("_".charAt(0) == ch) {
                flag = true;
            } else {
                if (flag) {
                    result.append(Character.toUpperCase(ch));
                    flag = false;
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    // ---------------------- substring ----------------------

    /**
     * 从指定位置截取到字符串末尾
     *
     * <p>起始位置小于 0 时按 0 处理；大于字符串长度时返回空串；等于长度时返回空串。
     * 越界不抛异常。</p>
     *
     * <pre>
     *      StringTool.substring(null, *)   = null
     *      StringTool.substring("", *)     = ""
     *      StringTool.substring("abc", 0)  = "abc"
     *      StringTool.substring("abc", 2)  = "c"
     *      StringTool.substring("abc", 4)  = ""
     *      StringTool.substring("abc", -2) = "abc"
     * </pre>
     *
     * @param str   待截取的字符串，可为 {@code null}
     * @param start 起始位置（包含）；为负数时按 0 处理
     * @return 截取结果；入参为 {@code null} 时返回 {@code null}，越界时返回 {@code ""}
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY;
        }

        return str.substring(start);
    }

    /**
     * 截取 {@code [start, end)} 区间
     *
     * <p>start 小于 0 时按 0 处理，大于字符串长度时返回空串；end 大于字符串长度时按长度处理；
     * end 小于等于 start 时返回空串。越界不抛异常。</p>
     *
     * <pre>
     *      StringTool.substring(null, *, *)    = null
     *      StringTool.substring("", *, *)      = ""
     *      StringTool.substring("abc", 1, 2)   = "b"
     *      StringTool.substring("abc", -1, 2)  = "ab"
     *      StringTool.substring("abc", 1, 0)   = ""
     *      StringTool.substring("abc", 1, 5)   = "bc"
     *      StringTool.substring("abc", 2, 1)   = ""
     * </pre>
     *
     * @param str   待截取的字符串，可为 {@code null}
     * @param start 起始位置（包含）；为负数时按 0 处理
     * @param end   结束位置（不包含）；为负数时结果为 {@code ""}
     * @return 截取结果；入参为 {@code null} 时返回 {@code null}，区间无效时返回 {@code ""}
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY;
        }

        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return EMPTY;
        }

        return str.substring(start, end);
    }


    // ---------------------- split、join ----------------------

    /**
     * 按分隔符分割字符串，默认裁剪并忽略空白片段
     *
     * <p>等价于 {@code split(str, separator, true, true)}。</p>
     *
     * <pre>
     *     StringTool.split("a,b,c", ",")     = ["a","b","c"]
     *     StringTool.split("a,b,", ",")      = ["a","b"]
     *     StringTool.split("a, ,c", ",")     = ["a","c"]
     * </pre>
     *
     * @param str       待分割字符串，可为 {@code null}
     * @param separator 分隔符，按正则表达式处理；为 {@code null} 或空白时不做分割
     * @return 分割后的列表；入参为 {@code null}、空串或空白串时返回 {@code null}
     */
    public static List<String> split(final String str, final String separator) {
        return split(str, separator, true, true);
    }

    /**
     * 按分隔符分割字符串，可控制裁剪与空白片段过滤
     *
     * <p>分隔符按正则表达式处理（{@link String#split(String)}），{@code "."}、{@code "|"} 等
     * 正则元字符需要转义。{@code separator} 为 {@code null} 或空白时，将 {@code str.trim()}
     * 作为单元素列表返回。</p>
     *
     * <pre>
     *     StringTool.split("a,b,c", ",")       = ["a","b","c"]
     *     StringTool.split("a, b ,c", ",")     = ["a","b","c"]
     *     StringTool.split("a,,c", ",")        = ["a","c"]
     * </pre>
     *
     * @param str               待分割字符串，可为 {@code null}
     * @param separator         分隔符，按正则表达式处理；为 {@code null} 或空白时不分割
     * @param trimTokens        是否对每个片段去除首尾空白
     * @param ignoreBlackTokens 是否丢弃空白片段
     * @return 分割后的列表；入参 {@code str} 为 {@code null}、空串或空白串时返回 {@code null}
     */
    public static List<String> split(final String str, final String separator, boolean trimTokens, boolean ignoreBlackTokens) {
        if (isBlank(str)) {
            return null;
        }
        if (isBlank(separator)) {
            return List.of(str.trim());
        }

        List<String> list = new ArrayList<>();
        for (String item : str.split(separator)) {
            if (trimTokens) {
                item = item.trim();
            }
            if (ignoreBlackTokens && isBlank(item)) {
                continue;
            }
            list.add(item);
        }
        return list;
    }

    /**
     * 使用分隔符拼接列表，默认裁剪并忽略空白元素
     *
     * <p>等价于 {@code join(list, separator, true, true)}。{@code null} 与空白元素会被跳过，
     * 且不会产生多余的分隔符。</p>
     *
     * <pre>
     *     StringTool.join(["a","b","c"], ",")     = "a,b,c"
     *     StringTool.join(["a","b"," c "], ",")   = "a,b,c"
     *     StringTool.join(["a","b",""], ",")      = "a,b"
     *     StringTool.join(["a",null,"c"], ",")    = "a,c"
     * </pre>
     *
     * @param list      待拼接列表，可为 {@code null}
     * @param separator 元素之间的分隔符；为 {@code null} 时按空串处理
     * @return 拼接结果；列表为 {@code null} 或空时返回 {@code null}
     */
    public static String join(final List<String> list, String separator) {
        return join(list, separator, true, true);
    }

    /**
     * 使用分隔符拼接列表，可控制裁剪与空白元素过滤
     *
     * <p>{@code ignoreBlackTokens} 为 {@code true} 时跳过 {@code null} 与空白元素，
     * 仅在实际拼接的元素之间插入分隔符。若为 {@code false}，{@code null} 元素在
     * {@code trimTokens} 为 {@code true} 时会触发 {@link NullPointerException}，
     * 否则按字符串 {@code "null"} 拼接。</p>
     *
     * @param list              待拼接列表，可为 {@code null}
     * @param separator         元素之间的分隔符；为 {@code null} 时按空串处理
     * @param trimTokens        是否对每个元素去除首尾空白
     * @param ignoreBlackTokens 是否忽略 {@code null} 与空白元素
     * @return 拼接结果；列表为 {@code null} 或空时返回 {@code null}，所有元素都被忽略时返回 {@code ""}
     */
    public static String join(final List<String> list, String separator, boolean trimTokens, boolean ignoreBlackTokens) {
        if (CollectionTool.isEmpty(list)) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }

        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            // parse token
            if (ignoreBlackTokens && isBlank(item)) {
                continue;
            }
            String token = trimTokens?item.trim():item;
            // separator
            if (first) {
                first = false;
            } else {
                buf.append(separator);
            }
            // append
            buf.append(token);
        }
        return buf.toString();
    }

    // ---------------------- format ----------------------

    /**
     * 使用 {@link MessageFormat} 格式化字符串
     *
     * <p>占位符形式为 {@code {0}}、{@code {1}}…，支持 {@code number}、{@code currency}、
     * {@code percent} 等格式。格式化结果依赖默认 Locale，数字、货币与百分比输出随 Locale 变化；
     * 模板中的单引号需按 {@link MessageFormat} 规则转义。{@code params} 为 {@code null}
     * 时不做替换，模板原样返回。</p>
     *
     * <pre>
     *     StringTool.format("hello,{0}!", "world")                                    = hello,world!
     *     StringTool.format("hello,{0}!", null)                                       = hello,{0}!
     *     StringTool.format("hello,{0}!")                                             = hello,{0}!
     *     StringTool.format("hello,{0}!", "world", "world")                           = hello,world!
     *     StringTool.format("Hello {0}, welcome {1}!")                                = Hello {0}, welcome {1}!
     *     StringTool.format("Hello {0}, welcome {1}!", null)                          = Hello {0}, welcome {1}!
     *     StringTool.format("Hello {0}, welcome {1}!", null, null)                    = Hello null, welcome null!
     *     StringTool.format("Hello {0}, welcome {1}!", "Alice")                       = Hello Alice, welcome {1}!
     *     StringTool.format("Hello {0}, welcome {1}!", "Alice", "Jack")               = Hello Alice, welcome Jack!
     *     StringTool.format("Hello {0}, welcome {1}!", "Alice", "Jack", "Lucy")       = Hello Alice, welcome Jack!
     *     StringTool.format("Hello {0}, you have {1} messages", "Alice", 5)           = Hello Alice, you have 5 messages
     *     StringTool.format("{1} messages for {0}", "Alice", 5)                       = 5 messages for Alice
     *     StringTool.format("Hello {0}, welcome {0}!", "Alice")                       = Hello Alice, welcome Alice!
     *     StringTool.format("Balance: {0,number}", 1234.56)                           = Balance: 1,234.56
     *     StringTool.format("Price: {0,number,currency}", 1234.56)                    = Price: ¥1,234.56
     *     StringTool.format("Success rate: {0,number,percent}", 0.85)                 = Success rate: 85%
     *     StringTool.format("Account: {0,number,#,##0.00}", 1234.5)                   = Account: 1,234.50
     * </pre>
     *
     * @param template 模板字符串，不可为 {@code null}
     * @param params   占位符参数数组，可为 {@code null}（此时不替换）
     * @return 格式化后的字符串
     */
    public static String format(String template, Object... params) {
        return MessageFormat.format(template, params);
    }

    /**
     * 使用 Map 按名称替换占位符
     *
     * <p>占位符形式为 {@code {key}}，按字面量替换（非正则、非 {@link MessageFormat}）。
     * 值为 {@code null} 的条目跳过，对应占位符保留。替换按 Map 迭代顺序逐条进行，
     * 若替换值中本身包含后续占位符文本，可能被再次替换。</p>
     *
     * <pre>
     *     StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack", "age", 18))        = jack is 18 years old
     *     StringTool.formatWithMap("{name} is {age} years old", null)                                             = {name} is {age} years old
     *     StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack"))                   = jack is {age} years old
     *     StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack", "age", null))      = jack is {age} years old
     * </pre>
     *
     * @param template 模板字符串，可为 {@code null}
     * @param params   占位符名称与取值的映射，可为 {@code null}
     * @return 替换后的字符串；模板为 {@code null}、空串或空白串，或 {@code params} 为 {@code null}/空时原样返回模板
     */
    public static String formatWithMap(String template, Map<String, Object> params) {
        if (isBlank(template) || MapTool.isEmpty(params)) {
            return template;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            // null value, will not be replaced
            if (entry.getValue() == null) {
                continue;
            }

            // do replace
            String oldPattern = "{" + entry.getKey() + "}";
            String newPattern = entry.getValue().toString();
            template = replace(template, oldPattern, newPattern);
        }
        return template;
    }

    // ---------------------- replace ----------------------

    /**
     * 文本替换（字面量）
     *
     * <p>使用 {@link String#replace(CharSequence, CharSequence)} 替换全部匹配内容，按字面量匹配，
     * 不走正则。旧串为 {@code null} 或空串、或新串为 {@code null} 时不做替换。</p>
     *
     * <pre>
     *     StringTool.replace("hello jack, how are you", "jack", "lucy")              = hello lucy, how are you
     *     StringTool.replace("hello jack, how are you, jack", "jack", "lucy")        = hello lucy, how are you, lucy
     *     StringTool.replace("", "jack", "lucy")                                     =
     *     StringTool.replace(null, "jack", "lucy")                                   = null
     *     StringTool.replace("hello jack, how are you", null, "jack")                = hello jack, how are you
     *     StringTool.replace("hello jack, how are you", "", "jack")                  = hello jack, how are you
     *     StringTool.replace("hello jack, how are you", " ", "-")                    = hello-jack,-how-are-you
     *     StringTool.replace("hello jack, how are you", "jack", null)                = hello jack, how are you
     *     StringTool.replace("hello jack, how are you", "jack", "")                  = hello , how are you
     *     StringTool.replace("hello jack, how are you", "jack", " ")                 = hello  , how are you
     * </pre>
     *
     * @param inString   原字符串，可为 {@code null}
     * @param oldPattern 需要被替换的旧串；为 {@code null} 或空串时原样返回
     * @param newPattern 替换后的新串；为 {@code null} 时原样返回
     * @return 替换后的字符串；未发生替换时返回原字符串（可能为 {@code null}）
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (isEmpty(inString) || isEmpty(oldPattern) || newPattern ==  null) {
            return inString;
        }
        return inString.replace(oldPattern, newPattern);
    }

    // ---------------------- remove prefix、suffix ----------------------

    /**
     * 移除前缀
     *
     * <p>仅当前缀完全匹配时才移除；前缀为 {@code null} 或空白串时不做处理。</p>
     *
     * <pre>
     *     StringTool.removePrefix("hello,world", "hello")                         =  ,world
     *     StringTool.removePrefix("hello,world", "world")                         =  hello,world
     *     StringTool.removePrefix("hello,world", "hello,world")                   =
     *     StringTool.removePrefix("hello,world", "")                              =  hello,world
     *     StringTool.removePrefix("hello,world", null)                            =  hello,world
     *     StringTool.removePrefix("", "world")                                    =
     *     StringTool.removePrefix(null, "world")                                  =  null
     * </pre>
     *
     * @param str    待处理字符串，可为 {@code null}
     * @param prefix 需要移除的前缀；为 {@code null} 或空白时原样返回
     * @return 移除前缀后的字符串；前缀不匹配或入参为 {@code null} 时原样返回
     */
    public static String removePrefix(String str, String prefix) {
        if (str == null || StringTool.isBlank(prefix)) {
            return str;
        }
        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    /**
     * 移除后缀
     *
     * <p>仅当后缀完全匹配时才移除；后缀为 {@code null} 或空白串时不做处理。</p>
     *
     * <pre>
     *     StringTool.removeSuffix("hello,world", "hello")                         = hello,world
     *     StringTool.removeSuffix("hello,world", "world")                         = hello,
     *     StringTool.removeSuffix("hello,world", "hello,world")                   =
     *     StringTool.removeSuffix("hello,world", "")                              = hello,world
     *     StringTool.removeSuffix("hello,world", null)                            = hello,world
     *     StringTool.removeSuffix("", "world")                                    =
     *     StringTool.removeSuffix(null, "world")                                  = null
     * </pre>
     *
     * @param str    待处理字符串，可为 {@code null}
     * @param suffix 需要移除的后缀；为 {@code null} 或空白时原样返回
     * @return 移除后缀后的字符串；后缀不匹配或入参为 {@code null} 时原样返回
     */
    public static String removeSuffix(String str, String suffix) {
        if (str == null || StringTool.isBlank(suffix)) {
            return str;
        }
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    // ---------------------- other ----------------------

    /**
     * 空安全比较两个字符串
     *
     * <p>区分大小写；两个参数均为 {@code null} 时返回 {@code true}。</p>
     *
     * <pre>
     *     StringTool.equals("hello", "hello")                   = true
     *     StringTool.equals("hello", "world")                   = false
     *     StringTool.equals(null, null)                         = true
     *     StringTool.equals(null, "world")                      = false
     *     StringTool.equals("hello", null)                      = false
     * </pre>
     *
     * @param str1 待比较的第一个字符串，可为 {@code null}
     * @param str2 待比较的第二个字符串，可为 {@code null}
     * @return 内容相同或同时为 {@code null} 时返回 {@code true}，否则返回 {@code false}
     */
    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }


    // ---------------------- other ----------------------

}
