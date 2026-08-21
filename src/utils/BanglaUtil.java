package utils;

/**
 * Utility functions for Bengali numerals and Unicode helpers.
 */
public class BanglaUtil {

    /**
     * Converts an integer or number to Bengali numeral string (e.g., 42 -> "৪২").
     */
    public static String toBanglaNum(long num) {
        return toBanglaString(String.valueOf(num));
    }

    /**
     * Converts any string containing ASCII digits (0-9) to Bengali digits (০-৯).
     */
    public static String toBanglaString(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                sb.append((char) ('\u09E6' + (ch - '0')));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
