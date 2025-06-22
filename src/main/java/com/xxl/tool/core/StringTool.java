package com.xxl.tool.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

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


    // ---------------------- array ----------------------

    /**
     * tokenize to string array
     *
     * <pre>
     *     StringTool.tokenizeToArray("a,b,c", ",", true, true)       = ["a","b","c"]
     *     StringTool.tokenizeToArray("a,b ,c, ", ",", true, true)    = ["a","b","c"]
     *     StringTool.tokenizeToArray("a,b ,c, ", ",", true, false) = ["a","b","c"," "]
     * </pre>
     *
     * @param str                   string to tokenize
     * @param delimiters            the delimiters to use for separating tokens
     * @return
     */
    public static String[] tokenizeToArray(final String str, String delimiters) {
        return tokenizeToArray(str, delimiters, true, true);
    }

    /**
     * tokenize to string array
     *
     * <pre>
     *     StringTool.tokenizeToArray("a,b,c", ",", true, true)       = ["a","b","c"]
     *     StringTool.tokenizeToArray("a,b ,c, ", ",", true, true)    = ["a","b","c"]
     *     StringTool.tokenizeToArray("a,b ,c, ", ",", true, false) = ["a","b","c"," "]
     * </pre>
     *
     * @param str                   string to tokenize
     * @param delimiters            the delimiters to use for separating tokens
     * @param trimTokens            if true, tokens will be trimmed
     * @param ignoreEmptyTokens     if true, empty tokens will be ignored
     * @return
     */
    public static String[] tokenizeToArray(final String str,
                                                 String delimiters,
                                                 boolean trimTokens,
                                                 boolean ignoreEmptyTokens) {
        // valid
        if (str == null) {
            return null;
        }

        // tokenize
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (ignoreEmptyTokens && token.isEmpty()) {
                continue;
            }
            tokens.add(token);
        }
        return toStringArray(tokens);
    }

    /**
     * convert collection to string array
     *
     * @param collection    collection to convert
     * @return              the array of strings, or null if the collection was null
     */
    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }
        return collection.toArray(new String[collection.size()]);
    }

    // ---------------------- other ----------------------

}
