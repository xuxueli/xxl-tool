package com.xxl.tool.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * string tool
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
     * Represents a failed index search.
     */
    public static final int INDEX_NOT_FOUND = -1;


    // ---------------------- empty ----------------------

    /**
     * is empty
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * is not empty
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !StringTool.isEmpty(str);
    }


    // ---------------------- blank ----------------------

    /**
     * is blank
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * is not blank
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str
     * @return
     */
    public static boolean isNotBlank(String str) {
        return !StringTool.isBlank(str);
    }


    // ---------------------- trim ----------------------

    /**
     * trim
     *
     * <pre>
     * StringUtils.trim(null)          = null
     * StringUtils.trim("")            = ""
     * StringUtils.trim("     ")       = ""
     * StringUtils.trim("abc")         = "abc"
     * StringUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str
     * @return
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * trimToNull
     *
     * <pre>
     * StringUtils.trimToNull(null)          = null
     * StringUtils.trimToNull("")            = null
     * StringUtils.trimToNull("     ")       = null
     * StringUtils.trimToNull("abc")         = "abc"
     * StringUtils.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str
     * @return
     */
    public static String trimToNull(String str) {
        String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    /**
     * trimToEmpty
     *
     * <pre>
     * StringUtils.trimToEmpty(null)          = ""
     * StringUtils.trimToEmpty("")            = ""
     * StringUtils.trimToEmpty("     ")       = ""
     * StringUtils.trimToEmpty("abc")         = "abc"
     * StringUtils.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str
     * @return
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }


    // ---------------------- isNumeric ----------------------

    /**
     * isNumeric
     *
     * <p>Checks if the String contains only unicode digits.
     * A decimal point is not a unicode digit and returns false.</p>
     *
     * <p><code>null</code> will return <code>false</code>.
     * An empty String (length()=0) will return <code>true</code>.</p>
     *
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = false
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if only contains digits, and is non-null
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
     * Count matches
     *
     * <p>Counts how many times the substring appears in the larger string.</p>
     *
     * <p>A {@code null} or empty ("") String input returns {@code 0}.</p>
     *
     * <pre>
     * StringUtils.countMatches(null, *)       = 0
     * StringUtils.countMatches("", *)         = 0
     * StringUtils.countMatches("abba", null)  = 0
     * StringUtils.countMatches("abba", "")    = 0
     * StringUtils.countMatches("abba", "a")   = 2
     * StringUtils.countMatches("abba", "ab")  = 1
     * StringUtils.countMatches("abba", "xxx") = 0
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param sub  the substring to count, may be null
     * @return the number of occurrences, 0 if either CharSequence is {@code null}
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
     * upperCase first letter
     *
     * <pre>
     *      StringUtils.upperCaseFirst(null, *)     = null
     *      StringUtils.upperCaseFirst("", *)       = ""
     *      StringUtils.countMatches("abc", *)      = "Abc"
     * </pre>
     *
     * @param text
     * @return
     */
    public static String upperCaseFirst(String text) {
        if (isBlank(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * lowerCase first letter
     *
     * <pre>
     *      StringUtils.lowerCaseFirst(null, *)     = null
     *      StringUtils.lowerCaseFirst("", *)       = ""
     *      StringUtils.lowerCaseFirst("ABC", *)    = "aBC"
     * </pre>
     *
     * @param text
     * @return
     */
    public static String lowerCaseFirst(String text) {
        if (isBlank(text)) {
            return text;
        }
        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    /**
     * convert underscores to hump format
     *
     * <pre>
     *      StringUtils.lowerCaseFirst(null, *)             = null
     *      StringUtils.lowerCaseFirst("", *)               = ""
     *      StringUtils.lowerCaseFirst("aaa_bbb", *)        = "aaaBbb"
     * </pre>
     *
     * @param underscoreText
     * @return
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
     * substring from start position
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
     * @param str       the String to get the substring from, may be null
     * @param start     the position to start from, negative means
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
     * substring from start position to end position
     *
     * <pre>
     *      StringTool.substring(null, *, *)    = null
     *      StringTool.substring("", * ,  *)    = "";
     *      StringTool.substring("abc", 1, 2)   = "b"
     *      StringTool.substring("abc", -1, 2)   = "ab"
     *      StringTool.substring("abc", 1, 0)   = ""
     *      StringTool.substring("abc", 1, 5)   = "bc"
     *      StringTool.substring("abc", 2, 1)   = ""
     * </pre>
     *
     * @param str       the String to get the substring from, may be null
     * @param start     the position to start from, negative means
     * @param end       the position to end at (exclusive), negative means
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
     * split str 2 array, with separator
     *
     * <pre>
     *     StringTool.split("a,b,c", ",")     = ["a","b","c"]
     *     StringTool.split("a,b,", ",")      = ["a","b"]
     *     StringTool.split("a, ,c", ",")     = ["a","c"]
     * </pre>
     *
     * @param str           string to split
     * @param separator     separator to use for separating elements
     * @return
     */
    public static List<String> split(final String str, final String separator) {
        return split(str, separator, true, true);
    }

    /**
     * split str 2 array, with separator
     *
     * <pre>
     *     StringTool.split("a,b,c", ",")       = ["a","b","c"]
     *     StringTool.split("a, b ,c", ",")     = ["a","b","c"]
     *     StringTool.split("a,,c", ",")        = ["a","c"]
     * </pre>
     *
     * @param str                   string to split
     * @param separator             separator to use for separating elements
     * @param trimTokens            true if the tokens should be trimmed
     * @param ignoreBlackTokens     true if empty tokens should be removed from the result
     * @return
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
     * join array to string
     *
     * <pre>
     *     StringTool.join(["a","b","c"], ",")     = "a,b,c"
     *     StringTool.join(["a","b"," c "], ",")   = "a,b,c"
     *     StringTool.join(["a","b",""], ",")      = "a,b"
     *     StringTool.join(["a",null,"c"], ",")    = "a,c"
     * </pre>
     *
     * @param list          list to join
     * @param separator     separator to use between elements
     * @return
     */
    public static String join(final List<String> list, String separator) {
        return join(list, separator, true, true);
    }

    /**
     * join array to string
     *
     * @param list                  list to join
     * @param separator             separator to use between elements
     * @param trimTokens            true if the tokens should be trimmed
     * @param ignoreBlackTokens     true if empty tokens should be ignored
     * @return
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
     * format string
     *
     * <pre>
     *     StringTool.format("hello,{0}!", "world"));                                    = hello,world!
     *     StringTool.format("hello,{0}!", null));                                       = hello,{0}!
     *     StringTool.format("hello,{0}!"));                                             = hello,{0}!
     *     StringTool.format("hello,{0}!", "world", "world"));                           = hello,world!
     *     StringTool.format("Hello {0}, welcome {1}!"));                                = Hello {0}, welcome {1}!
     *     StringTool.format("Hello {0}, welcome {1}!",null));                           = Hello {0}, welcome {1}!
     *     StringTool.format("Hello {0}, welcome {1}!",null, null));                     = Hello null, welcome null!
     *     StringTool.format("Hello {0}, welcome {1}!", "Alice"));                       = Hello Alice, welcome {1}!
     *     StringTool.format("Hello {0}, welcome {1}!", "Alice", "Jack"));               = Hello Alice, welcome Jack!
     *     StringTool.format("Hello {0}, welcome {1}!", "Alice", "Jack", "Lucy"));       = Hello Alice, welcome Jack!
     *     StringTool.format("Hello {0}, you have {1} messages", "Alice", 5));           = Hello Alice, you have 5 messages
     *     StringTool.format("{1} messages for {0}", "Alice", 5));                       = 5 messages for Alice
     *     StringTool.format("Hello {0}, welcome {0}!", "Alice"));                       = Hello Alice, welcome Alice!
     *     StringTool.format("Balance: {0,number}", 1234.56));                           = Balance: 1,234.56
     *     StringTool.format("Price: {0,number,currency}", 1234.56));                    = Price: ¥1,234.56
     *     StringTool.format("Success rate: {0,number,percent}", 0.85));                 = Success rate: 85%
     *     StringTool.format("Account: {0,number,#,##0.00}", 1234.5));                   = Account: 1,234.50
     * </pre>
     *
     * @param template  template string
     * @param params    string array
     * @return
     */
    public static String format(String template, Object... params) {
        return MessageFormat.format(template, params);
    }

    /**
     * format string with map
     * <pre>
     *     StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack", "age", 18))        = jack is 18 years old
     *     StringTool.formatWithMap("{name} is {age} years old", null)                                             = {name} is {age} years old
     *     StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack"))                   = jack is {age} years old
     *     StringTool.formatWithMap("{name} is {age} years old", MapTool.newMap("name", "jack", "age", null))      = jack is {age} years old
     * </pre>
     *
     * @param template  template string
     * @param params    parameter map
     * @return
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
     * replace string
     *
     * <pre>
     *     StringTool.replace("hello jack, how are you", "jack", "lucy"));              = hello lucy, how are you
     *     StringTool.replace("hello jack, how are you, jack", "jack", "lucy"));        = hello lucy, how are you, lucy
     *     StringTool.replace("", "jack", "lucy"));                                     =
     *     StringTool.replace(null, "jack", "lucy"));                                   = null
     *     StringTool.replace("hello jack, how are you", null, "jack"));                = hello jack, how are you
     *     StringTool.replace("hello jack, how are you", "", "jack"));                  = hello jack, how are you
     *     StringTool.replace("hello jack, how are you", " ", "-"));                    = hello-jack,-how-are-you
     *     StringTool.replace("hello jack, how are you", "jack", null));                = hello jack, how are you
     *     StringTool.replace("hello jack, how are you", "jack", ""));                  = hello , how are you
     *     StringTool.replace("hello jack, how are you", "jack", " "));                 = hello  , how are you
     * </pre>
     *
     * @param inString      input string
     * @param oldPattern    old pattern, empty string will not be replaced
     * @param newPattern    new pattern, null string will not to replace
     * @return
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (isEmpty(inString) || isEmpty(oldPattern) || newPattern ==  null) {
            return inString;
        }
        return inString.replace(oldPattern, newPattern);
    }

    // ---------------------- remove prefix、suffix ----------------------

    /**
     * remove prefix
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
     * @param str       the string to remove prefix
     * @param prefix    prefix to remove
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
     * remove suffix
     *
     * <pre>
     *     StringTool.removeSuffix("hello,world", "hello")                          hello,world
     *     StringTool.removeSuffix("hello,world", "world")                          hello,
     *     StringTool.removeSuffix("hello,world", "hello,world"))
     *     StringTool.removeSuffix("hello,world", "")                               hello,world
     *     StringTool.removeSuffix("hello,world", null)                             hello,world
     *     StringTool.removeSuffix("", "world")
     *     StringTool.removeSuffix(null, "world")                                   null
     * </pre>
     * @param str       the string to remove suffix
     * @param suffix    suffix to remove
     * @return
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
     * string equals
     *
     * <pre>
     *     StringTool.equals("hello", "hello")                   = true
     *     StringTool.equals("hello", "world")                   = false
     *     StringTool.equals(null, null)                         = true
     *     StringTool.equals(null, "world")                      = false
     *     StringTool.equals("hello", null)                      = false
     * </pre>
     *
     * @param str1  the first string to compare
     * @param str2  the second string to compare
     * @return true if equals
     */
    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }


    // ---------------------- other ----------------------

}
