package com.xxl.tool.core;

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
     * StringUtils.isNumeric("")     = true
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
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
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

    // ---------------------- Count matches ----------------------

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


}
