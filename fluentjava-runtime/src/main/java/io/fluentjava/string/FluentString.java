package io.fluentjava.string;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Fluent utility methods for {@link java.lang.String}.
 *
 * <p>All methods are {@code public static}, null-safe, and side-effect free.
 * They are the compile-time rewrite targets for FluentJava's javac plugin:
 * the plugin rewrites {@code "test".isBlankSafe()} into
 * {@code FluentString.isBlankSafe("test")} in the AST before type resolution.</p>
 *
 * <h3>Fluent usage (with javac plugin):</h3>
 * <pre>{@code
 *   "test".isBlankSafe()     // â†’ FluentString.isBlankSafe("test")
 *   "42".toIntOrNull()       // â†’ FluentString.toIntOrNull("42")
 *   " hello ".trimToNull()   // â†’ FluentString.trimToNull(" hello ")
 * }</pre>
 *
 * <h3>Direct static usage (without plugin):</h3>
 * <pre>{@code
 *   FluentString.isBlankSafe(null);     // true
 *   FluentString.toIntOrNull("42");     // 42
 *   FluentString.trimToNull("  hi  "); // "hi"
 * }</pre>
 *
 * <h3>Security:</h3>
 * <ul>
 *   <li>Zero reflection</li>
 *   <li>Zero internal JDK API usage at runtime</li>
 *   <li>Zero mutable state</li>
 *   <li>Zero external dependencies</li>
 * </ul>
 *
 * @since 1.0.0
 */
public final class FluentString {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://[\\w\\-]+(\\.[\\w\\-]+)+([/\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]*)?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    private static final char ELLIPSIS = '\u2026';

    /** Utility class â€” cannot be instantiated. */
    private FluentString() {
        throw new AssertionError("FluentString is a utility class and cannot be instantiated");
    }

    // --- isBlankSafe ---

    /**
     * Null-safe blank check.
     *
     * <p>Returns {@code true} if the string is {@code null}, empty ({@code ""}),
     * or contains only whitespace characters.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.isBlankSafe(null)   // true
     *   FluentString.isBlankSafe("")     // true
     *   FluentString.isBlankSafe("  ")   // true
     *   FluentString.isBlankSafe("abc")  // false
     * }</pre>
     *
     * <h4>Fluent equivalent:</h4>
     * <pre>{@code
     *   "test".isBlankSafe()  // â†’ FluentString.isBlankSafe("test")
     * }</pre>
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if the string is null, empty, or blank
     */
    public static boolean isBlankSafe(String s) {
        return isBlank(s);
    }

    /**
     * Checks if a string is null or blank (only whitespace).
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if the string is null, empty, or contains only whitespace
     */
    public static boolean isNullOrBlank(String s) {
        return isBlank(s);
    }

    /**
     * Checks if a string is null or empty (zero length).
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if the string is null or empty
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Returns the string itself, or an empty string if it is null.
     *
     * @param s the string to check (may be {@code null})
     * @return the original string, or {@code ""} if null
     */
    public static String orEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * Returns the string if not null or blank, otherwise returns the default value.
     *
     * @param s the string to check (may be {@code null})
     * @param defaultValue the value to return if s is null or blank
     * @return the original string if not null/blank, otherwise {@code defaultValue}
     */
    public static String orDefault(String s, String defaultValue) {
        return isBlank(s) ? defaultValue : s;
    }

    /**
     * Trims the string and returns {@code null} if the result is empty.
     *
     * <p>Useful for normalizing user input: blank values are treated as absent
     * ({@code null}) rather than empty strings.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.trimToNull("  hello  ") // "hello"
     *   FluentString.trimToNull("  ")        // null
     *   FluentString.trimToNull("")          // null
     *   FluentString.trimToNull(null)        // null
     * }</pre>
     *
     * @param s the string to trim (may be {@code null})
     * @return the trimmed string, or {@code null} if the result would be empty
     */
    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Parses the string as an integer, returning {@code null} on failure.
     *
     * <p>Eliminates the verbose try-catch pattern for integer parsing.
     * Leading/trailing whitespace is trimmed before parsing.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.toIntOrNull("42")    // 42
     *   FluentString.toIntOrNull("-7")    // -7
     *   FluentString.toIntOrNull("  42 ") // 42  (trimmed)
     *   FluentString.toIntOrNull("abc")   // null
     *   FluentString.toIntOrNull(null)    // null
     *   FluentString.toIntOrNull("")      // null
     * }</pre>
     *
     * @param s the string to parse (may be {@code null})
     * @return the parsed {@link Integer}, or {@code null} if parsing fails
     */
    public static Integer toIntOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses the string as a long, returning {@code null} on failure.
     *
     * @param s the string to parse (may be {@code null})
     * @return the parsed {@link Long}, or {@code null} if parsing fails
     */
    public static Long toLongOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses the string as a double, returning {@code null} on failure.
     *
     * @param s the string to parse (may be {@code null})
     * @return the parsed {@link Double}, or {@code null} if parsing fails
     */
    public static Double toDoubleOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses the string as a boolean, returning {@code null} if not recognized.
     * Accepts "true", "yes", "1" for true and "false", "no", "0" for false (case-insensitive).
     *
     * @param s the string to parse (may be {@code null})
     * @return the parsed {@link Boolean}, or {@code null} if not a recognized boolean value
     */
    public static Boolean toBooleanOrNull(String s) {
        if (s == null) {
            return null;
        }
        String value = s.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "true", "yes", "1" -> Boolean.TRUE;
            case "false", "no", "0" -> Boolean.FALSE;
            default -> null;
        };
    }

    /**
     * Parses the string as a {@link BigDecimal}, returning {@code null} on failure.
     *
     * @param s the string to parse (may be {@code null})
     * @return the parsed {@link BigDecimal}, or {@code null} if parsing fails
     */
    public static BigDecimal toBigDecimalOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns the first n characters of the string, or the whole string if shorter.
     *
     * @param s the string to take from (may be {@code null})
     * @param n the number of characters to take
     * @return the substring of the first n characters, or null if s is null
     */
    public static String take(String s, int n) {
        if (s == null) {
            return null;
        }
        if (n <= 0) {
            return "";
        }
        return s.substring(0, Math.min(n, s.length()));
    }

    /**
     * Returns the last n characters of the string, or the whole string if shorter.
     *
     * @param s the string to take from (may be {@code null})
     * @param n the number of characters to take from the end
     * @return the substring of the last n characters, or null if s is null
     */
    public static String takeLast(String s, int n) {
        if (s == null) {
            return null;
        }
        if (n <= 0) {
            return "";
        }
        int len = s.length();
        if (n >= len) {
            return s;
        }
        return s.substring(len - n);
    }

    /**
     * Returns the string without the first n characters.
     *
     * @param s the string to drop from (may be {@code null})
     * @param n the number of characters to drop from the start
     * @return the substring without the first n characters, or null if s is null
     */
    public static String drop(String s, int n) {
        if (s == null) {
            return null;
        }
        if (n <= 0) {
            return s;
        }
        if (n >= s.length()) {
            return "";
        }
        return s.substring(n);
    }

    /**
     * Returns the string without the last n characters.
     *
     * @param s the string to drop from (may be {@code null})
     * @param n the number of characters to drop from the end
     * @return the substring without the last n characters, or null if s is null
     */
    public static String dropLast(String s, int n) {
        if (s == null) {
            return null;
        }
        if (n <= 0) {
            return s;
        }
        if (n >= s.length()) {
            return "";
        }
        return s.substring(0, s.length() - n);
    }

    /**
     * Pads the string at the start with the given character until it reaches the specified length.
     *
     * @param s the string to pad (may be {@code null})
     * @param len the desired total length
     * @param padChar the character to use for padding
     * @return the padded string, or null if s is null
     */
    public static String padStart(String s, int len, char padChar) {
        if (s == null) {
            return null;
        }
        if (len <= s.length()) {
            return s;
        }
        return repeatChar(padChar, len - s.length()) + s;
    }

    /**
     * Pads the string at the end with the given character until it reaches the specified length.
     *
     * @param s the string to pad (may be {@code null})
     * @param len the desired total length
     * @param padChar the character to use for padding
     * @return the padded string, or null if s is null
     */
    public static String padEnd(String s, int len, char padChar) {
        if (s == null) {
            return null;
        }
        if (len <= s.length()) {
            return s;
        }
        return s + repeatChar(padChar, len - s.length());
    }

    /**
     * Returns the reversed string.
     *
     * @param s the string to reverse (may be {@code null})
     * @return the reversed string, or null if s is null
     */
    public static String reversed(String s) {
        if (s == null) {
            return null;
        }
        return new StringBuilder(s).reverse().toString();
    }

    /**
     * Returns the string with the first character in uppercase and the rest in lowercase.
     *
     * @param s the string to capitalize (may be {@code null})
     * @return the capitalized string, or null if s is null
     */
    public static String capitalized(String s) {
        if (s == null) {
            return null;
        }
        if (s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT)
                + s.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the string with the first character in lowercase and the rest unchanged.
     *
     * @param s the string to decapitalize (may be {@code null})
     * @return the decapitalized string, or null if s is null
     */
    public static String decapitalized(String s) {
        if (s == null) {
            return null;
        }
        if (s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toLowerCase(Locale.ROOT) + s.substring(1);
    }

    /**
     * Truncates the string to a maximum length, appending a suffix if truncated.
     *
     * @param s the string to truncate (may be {@code null})
     * @param max the maximum length
     * @param suffix the suffix to append if truncated (may be {@code null})
     * @return the truncated string, or null if s is null
     */
    public static String truncate(String s, int max, String suffix) {
        if (s == null) {
            return null;
        }
        if (max <= 0) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        String safeSuffix = suffix == null ? "" : suffix;
        int cut = Math.max(0, max - safeSuffix.length());
        return s.substring(0, cut) + safeSuffix;
    }

    /**
     * Removes the given prefix from the string if present.
     *
     * @param s the string to process (may be {@code null})
     * @param prefix the prefix to remove (may be {@code null})
     * @return the string without the prefix, or null if s is null
     */
    public static String removePrefix(String s, String prefix) {
        if (s == null) {
            return null;
        }
        if (prefix == null || prefix.isEmpty()) {
            return s;
        }
        return s.startsWith(prefix) ? s.substring(prefix.length()) : s;
    }

    /**
     * Removes the given suffix from the string if present.
     *
     * @param s the string to process (may be {@code null})
     * @param suffix the suffix to remove (may be {@code null})
     * @return the string without the suffix, or null if s is null
     */
    public static String removeSuffix(String s, String suffix) {
        if (s == null) {
            return null;
        }
        if (suffix == null || suffix.isEmpty()) {
            return s;
        }
        return s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s;
    }

    /**
     * Wraps the string with the given string at the start and end.
     *
     * @param s the string to wrap (may be {@code null})
     * @param wrapWith the string to wrap with (may be {@code null})
     * @return the wrapped string, or null if s is null
     */
    public static String wrap(String s, String wrapWith) {
        if (s == null) {
            return null;
        }
        if (wrapWith == null || wrapWith.isEmpty()) {
            return s;
        }
        return wrapWith + s + wrapWith;
    }

    /**
     * Removes the given string from the start and end if present.
     *
     * @param s the string to unwrap (may be {@code null})
     * @param wrapWith the string to remove from both ends (may be {@code null})
     * @return the unwrapped string, or null if s is null
     */
    public static String unwrap(String s, String wrapWith) {
        if (s == null) {
            return null;
        }
        if (wrapWith == null || wrapWith.isEmpty()) {
            return s;
        }
        if (s.startsWith(wrapWith) && s.endsWith(wrapWith) && s.length() >= wrapWith.length() * 2) {
            return s.substring(wrapWith.length(), s.length() - wrapWith.length());
        }
        return s;
    }

    /**
     * Repeats the string n times.
     *
     * @param s the string to repeat (may be {@code null})
     * @param n the number of times to repeat
     * @return the repeated string, or null if s is null
     */
    public static String repeat(String s, int n) {
        if (s == null) {
            return null;
        }
        if (n <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * Checks if the string contains the given substring, ignoring case.
     *
     * @param s the string to search (may be {@code null})
     * @param sub the substring to find (may be {@code null})
     * @return {@code true} if s contains sub, ignoring case
     */
    public static boolean containsIgnoreCase(String s, String sub) {
        if (s == null || sub == null) {
            return false;
        }
        return s.toLowerCase(Locale.ROOT).contains(sub.toLowerCase(Locale.ROOT));
    }

    /**
     * Checks if the string starts with the given prefix, ignoring case.
     *
     * @param s the string to check (may be {@code null})
     * @param prefix the prefix to check (may be {@code null})
     * @return {@code true} if s starts with prefix, ignoring case
     */
    public static boolean startsWithIgnoreCase(String s, String prefix) {
        if (s == null || prefix == null) {
            return false;
        }
        if (prefix.length() > s.length()) {
            return false;
        }
        return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Checks if the string ends with the given suffix, ignoring case.
     *
     * @param s the string to check (may be {@code null})
     * @param suffix the suffix to check (may be {@code null})
     * @return {@code true} if s ends with suffix, ignoring case
     */
    public static boolean endsWithIgnoreCase(String s, String suffix) {
        if (s == null || suffix == null) {
            return false;
        }
        if (suffix.length() > s.length()) {
            return false;
        }
        int start = s.length() - suffix.length();
        return s.regionMatches(true, start, suffix, 0, suffix.length());
    }

    /**
     * Counts the number of times the substring appears in the string.
     *
     * @param s the string to search (may be {@code null})
     * @param sub the substring to count (may be {@code null})
     * @return the number of occurrences of sub in s
     */
    public static int countOccurrences(String s, String sub) {
        if (s == null || sub == null || sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = s.indexOf(sub, idx)) >= 0) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * Checks if the string contains only numeric digits.
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if s contains only digits
     */
    public static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the string contains only alphabetic letters.
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if s contains only letters
     */
    public static boolean isAlpha(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isLetter(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the string contains only alphanumeric characters (letters or digits).
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if s contains only letters or digits
     */
    public static boolean isAlphanumeric(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isLetterOrDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the string is a valid email address (simple regex).
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if s matches a basic email pattern
     */
    public static boolean isEmail(String s) {
        if (isBlank(s)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(s.trim()).matches();
    }

    /**
     * Converts the string to a URL-friendly slug (lowercase, ASCII, hyphens).
     *
     * @param s the string to convert (may be {@code null})
     * @return the slugified string, or null if s is null
     */
    public static String toSlug(String s) {
        if (s == null) {
            return null;
        }
        String normalized = stripAccents(s).toLowerCase(Locale.ROOT);
        String slug = normalized.replaceAll("[^a-z0-9]+", "-");
        return slug.replaceAll("^-+|-+$", "");
    }

    // --- mask ---

    /**
     * Masks the beginning of a string, leaving only the last {@code visible}
     * characters visible. Useful for masking sensitive data (credit card
     * numbers, phone numbers, SSNs, etc.).
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.mask("1234567890", 4) // "******7890"
     *   FluentString.mask("abc", 5)        // "abc"  (shorter than visible)
     *   FluentString.mask("abc", 0)        // "***"  (mask everything)
     *   FluentString.mask(null, 3)         // null
     *   FluentString.mask("", 3)           // ""
     * }</pre>
     *
     * @param s       the string to mask (may be {@code null})
     * @param visible the number of characters to leave visible at the end
     * @return the masked string, or the original if shorter than {@code visible}
     * @throws IllegalArgumentException if {@code visible} is negative
     */
    public static String mask(String s, int visible) {
        if (visible < 0) {
            throw new IllegalArgumentException("visible must be >= 0, got: " + visible);
        }
        if (s == null) {
            return null;
        }
        int len = s.length();
        if (len == 0 || visible >= len) {
            return s;
        }
        return repeatChar('*', len - visible) + s.substring(len - visible);
    }

    /**
     * Converts the string to camelCase (first word lowercase, next words capitalized).
     *
     * @param s the string to convert (may be {@code null})
     * @return the camelCase string, or null if s is null
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        if (isBlank(s)) {
            return s;
        }
        String[] parts = s.trim().split("[^a-zA-Z0-9]+");
        if (parts.length == 0) {
            return "";
        }
        StringBuilder out = new StringBuilder(parts[0].toLowerCase(Locale.ROOT));
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            out.append(parts[i].substring(0, 1).toUpperCase(Locale.ROOT));
            if (parts[i].length() > 1) {
                out.append(parts[i].substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return out.toString();
    }

    /**
     * Converts the string to snake_case (lowercase, underscores).
     *
     * @param s the string to convert (may be {@code null})
     * @return the snake_case string, or null if s is null
     */
    public static String toSnakeCase(String s) {
        if (s == null) {
            return null;
        }
        if (isBlank(s)) {
            return s;
        }
        String value = s
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .toLowerCase(Locale.ROOT);
        return value.replaceAll("^_+|_+$", "");
    }

    /**
     * Returns the leftmost len characters of the string, or the whole string if shorter.
     *
     * @param s the string to take from (may be {@code null})
     * @param len the number of characters to take from the start
     * @return the substring of the first len characters, or null if s is null
     */
    public static String left(String s, int len) {
        return take(s, len);
    }

    /**
     * Returns the rightmost len characters of the string, or the whole string if shorter.
     *
     * @param s the string to take from (may be {@code null})
     * @param len the number of characters to take from the end
     * @return the substring of the last len characters, or null if s is null
     */
    public static String right(String s, int len) {
        return takeLast(s, len);
    }

    /**
     * Centers the string in a field of the given size, padding with the given character.
     *
     * @param s the string to center (may be {@code null})
     * @param size the total width
     * @param pad the character to use for padding
     * @return the centered string, or null if s is null
     */
    public static String center(String s, int size, char pad) {
        if (s == null) {
            return null;
        }
        if (size <= s.length()) {
            return s;
        }
        int totalPad = size - s.length();
        int leftPad = totalPad / 2;
        int rightPad = totalPad - leftPad;
        return repeatChar(pad, leftPad) + s + repeatChar(pad, rightPad);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String repeatChar(char character, int count) {
        if (count <= 0) {
            return "";
        }
        char[] chars = new char[count];
        for (int i = 0; i < count; i++) {
            chars[i] = character;
        }
        return new String(chars);
    }

    /**
     * Replaces all sequences of whitespace in the string with a single space and trims the result.
     *
     * @param s the string to normalize (may be {@code null})
     * @return the normalized string, or null if s is null
     */
    public static String normalizeWhitespace(String s) {
        if (s == null) {
            return null;
        }
        return s.trim().replaceAll("\\s+", " ");
    }

    /**
     * Removes accents and diacritical marks from the string (Unicode normalization).
     *
     * @param s the string to process (may be {@code null})
     * @return the string without accents, or null if s is null
     */
    public static String stripAccents(String s) {
        if (s == null) {
            return null;
        }
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }

    // ────────────────────────────────────────────────────────────────
    // New methods
    // ────────────────────────────────────────────────────────────────

    /**
     * Returns {@code defaultValue} if the string is {@code null} or blank,
     * otherwise returns the string itself.
     *
     * <p>Inspired by Kotlin's {@code ifBlank} and Apache {@code StringUtils.defaultIfBlank}.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.ifBlank(null, "default")  // "default"
     *   FluentString.ifBlank("  ", "default")  // "default"
     *   FluentString.ifBlank("abc", "default") // "abc"
     * }</pre>
     *
     * @param s            the string to check (may be {@code null})
     * @param defaultValue the value to return if s is null or blank
     * @return the original string if not null/blank, otherwise {@code defaultValue}
     */
    public static String ifBlank(String s, String defaultValue) {
        return isBlank(s) ? defaultValue : s;
    }

    /**
     * Returns {@code defaultValue} if the string is {@code null} or empty,
     * otherwise returns the string itself.
     *
     * <p>Inspired by Kotlin's {@code ifEmpty}.</p>
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.ifEmpty(null, "default")  // "default"
     *   FluentString.ifEmpty("", "default")    // "default"
     *   FluentString.ifEmpty("  ", "default")  // "  "
     *   FluentString.ifEmpty("abc", "default") // "abc"
     * }</pre>
     *
     * @param s            the string to check (may be {@code null})
     * @param defaultValue the value to return if s is null or empty
     * @return the original string if not null/empty, otherwise {@code defaultValue}
     */
    public static String ifEmpty(String s, String defaultValue) {
        return (s == null || s.isEmpty()) ? defaultValue : s;
    }

    /**
     * Splits the string by the given delimiter and returns a list of non-empty parts.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.splitToList("a,b,c", ",")   // ["a", "b", "c"]
     *   FluentString.splitToList("a,,b", ",")     // ["a", "b"]
     *   FluentString.splitToList(null, ",")        // []
     * }</pre>
     *
     * @param s         the string to split (may be {@code null})
     * @param delimiter the delimiter pattern (must not be {@code null})
     * @return an unmodifiable list of non-empty parts
     */
    public static List<String> splitToList(String s, String delimiter) {
        if (s == null || s.isEmpty()) {
            return List.of();
        }
        String[] parts = s.split(Pattern.quote(delimiter), -1);
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Splits the string by line separators ({@code \n} or {@code \r\n}) and
     * returns the resulting lines.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.lines("a\nb\nc")     // ["a", "b", "c"]
     *   FluentString.lines("a\r\nb")      // ["a", "b"]
     *   FluentString.lines(null)           // []
     * }</pre>
     *
     * @param s the string to split (may be {@code null})
     * @return an unmodifiable list of lines
     */
    public static List<String> lines(String s) {
        if (s == null) {
            return List.of();
        }
        return List.of(s.split("\\r?\\n", -1));
    }

    /**
     * Encodes the string to Base64.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.toBase64("hello")  // "aGVsbG8="
     *   FluentString.toBase64(null)     // null
     * }</pre>
     *
     * @param s the string to encode (may be {@code null})
     * @return the Base64-encoded string, or {@code null} if s is null
     */
    public static String toBase64(String s) {
        if (s == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes the string from Base64.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.fromBase64("aGVsbG8=")  // "hello"
     *   FluentString.fromBase64("!!!")         // null (invalid)
     *   FluentString.fromBase64(null)          // null
     * }</pre>
     *
     * @param s the Base64-encoded string (may be {@code null})
     * @return the decoded string, or {@code null} if s is null or invalid
     */
    public static String fromBase64(String s) {
        if (s == null) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(s);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Truncates the string to {@code maxLength} and appends the unicode
     * ellipsis character ({@code \u2026}) if truncation occurred.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.ellipsize("Hello World", 8) // "Hello W…"
     *   FluentString.ellipsize("Hi", 10)          // "Hi"
     *   FluentString.ellipsize(null, 5)            // null
     * }</pre>
     *
     * @param s         the string to ellipsize (may be {@code null})
     * @param maxLength the maximum length including the ellipsis character
     * @return the ellipsized string, or {@code null} if s is null
     */
    public static String ellipsize(String s, int maxLength) {
        if (s == null) {
            return null;
        }
        if (maxLength <= 0) {
            return "";
        }
        if (s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, maxLength - 1) + ELLIPSIS;
    }

    /**
     * Converts the string to PascalCase.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.toPascalCase("hello world")   // "HelloWorld"
     *   FluentString.toPascalCase("hello_world")    // "HelloWorld"
     *   FluentString.toPascalCase("helloWorld")     // "HelloWorld"
     *   FluentString.toPascalCase(null)              // null
     * }</pre>
     *
     * @param s the string to convert (may be {@code null})
     * @return the PascalCase string, or {@code null} if s is null
     */
    public static String toPascalCase(String s) {
        if (s == null) {
            return null;
        }
        if (isBlank(s)) {
            return s;
        }
        // Split on camelCase boundaries and non-alphanumeric chars
        String spaced = s.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
        String[] parts = spaced.trim().split("[^a-zA-Z0-9]+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            out.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            if (part.length() > 1) {
                out.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return out.toString();
    }

    /**
     * Converts the string to kebab-case (lowercase with hyphens).
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.toKebabCase("helloWorld")    // "hello-world"
     *   FluentString.toKebabCase("Hello World")   // "hello-world"
     *   FluentString.toKebabCase("hello_world")   // "hello-world"
     *   FluentString.toKebabCase(null)             // null
     * }</pre>
     *
     * @param s the string to convert (may be {@code null})
     * @return the kebab-case string, or {@code null} if s is null
     */
    public static String toKebabCase(String s) {
        if (s == null) {
            return null;
        }
        if (isBlank(s)) {
            return s;
        }
        String value = s
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("[^a-zA-Z0-9]+", "-")
                .toLowerCase(Locale.ROOT);
        return value.replaceAll("^-+|-+$", "");
    }

    /**
     * Checks if the entire string matches the given regular expression (null-safe).
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.matchesPattern("abc123", "^[a-z0-9]+$")  // true
     *   FluentString.matchesPattern("abc!", "^[a-z]+$")        // false
     *   FluentString.matchesPattern(null, ".*")                 // false
     * }</pre>
     *
     * @param s     the string to test (may be {@code null})
     * @param regex the regular expression pattern
     * @return {@code true} if the entire string matches the regex
     */
    public static boolean matchesPattern(String s, String regex) {
        if (s == null || regex == null) {
            return false;
        }
        return s.matches(regex);
    }

    /**
     * Computes a cryptographic hash of the string using the specified algorithm
     * and returns the result as a lowercase hex string.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.digest("hello", "SHA-256") // "2cf24dba5fb0a30e..."
     *   FluentString.digest("hello", "MD5")     // "5d41402abc4b2a76..."
     *   FluentString.digest(null, "SHA-256")    // null
     * }</pre>
     *
     * @param s         the string to hash (may be {@code null})
     * @param algorithm the digest algorithm (e.g. "SHA-256", "MD5")
     * @return the hex-encoded hash, or {@code null} if s is null
     * @throws IllegalArgumentException if the algorithm is not available
     */
    public static String digest(String s, String algorithm) {
        if (s == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithm, e);
        }
    }

    /**
     * Extracts the initials from the string (first letter of each word, uppercase).
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.toInitials("John Doe")           // "JD"
     *   FluentString.toInitials("jean-pierre martin")  // "JM"
     *   FluentString.toInitials(null)                   // null
     * }</pre>
     *
     * @param s the string to extract initials from (may be {@code null})
     * @return the initials as an uppercase string, or {@code null} if s is null
     */
    public static String toInitials(String s) {
        if (s == null) {
            return null;
        }
        if (isBlank(s)) {
            return "";
        }
        String[] words = s.trim().split("[^a-zA-Z0-9]+");
        StringBuilder initials = new StringBuilder(words.length);
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return initials.toString();
    }

    /**
     * Counts the number of words in the string (separated by whitespace).
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.countWords("Hello World")  // 2
     *   FluentString.countWords("  a  b  c  ")  // 3
     *   FluentString.countWords(null)            // 0
     *   FluentString.countWords("")              // 0
     * }</pre>
     *
     * @param s the string to count words in (may be {@code null})
     * @return the number of words, 0 if null or blank
     */
    public static int countWords(String s) {
        if (isBlank(s)) {
            return 0;
        }
        return s.trim().split("\\s+").length;
    }

    /**
     * Checks if the string is a valid HTTP or HTTPS URL.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.isUrl("https://example.com")  // true
     *   FluentString.isUrl("ftp://example.com")     // false
     *   FluentString.isUrl(null)                     // false
     * }</pre>
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if the string is a valid HTTP(S) URL
     */
    public static boolean isUrl(String s) {
        if (isBlank(s)) {
            return false;
        }
        return URL_PATTERN.matcher(s.trim()).matches();
    }

    /**
     * Checks if the string is a valid IPv4 address (0.0.0.0 to 255.255.255.255).
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.isIPv4("192.168.1.1")  // true
     *   FluentString.isIPv4("999.1.1.1")    // false
     *   FluentString.isIPv4(null)            // false
     * }</pre>
     *
     * @param s the string to check (may be {@code null})
     * @return {@code true} if the string is a valid IPv4 address
     */
    public static boolean isIPv4(String s) {
        if (isBlank(s)) {
            return false;
        }
        return IPV4_PATTERN.matcher(s.trim()).matches();
    }

    /**
     * Partially redacts sensitive data. Keeps the first character and the last
     * 4 characters, replacing the middle with {@code ***}.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.redact("john@email.com")      // "j***@email.com"  (email-aware)
     *   FluentString.redact("4111111111111111")     // "4***1111"
     *   FluentString.redact("ab")                   // "ab"  (too short)
     *   FluentString.redact(null)                   // null
     * }</pre>
     *
     * @param s the string to redact (may be {@code null})
     * @return the redacted string, or {@code null} if s is null
     */
    public static String redact(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() <= 5) {
            return s;
        }
        // Email-aware redaction
        int atIdx = s.indexOf('@');
        if (atIdx > 0) {
            return s.charAt(0) + "***" + s.substring(atIdx);
        }
        // General redaction: keep first char + last 4 chars
        return s.charAt(0) + "***" + s.substring(s.length() - 4);
    }

    /**
     * Formats the string as a currency amount using the given currency code.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentString.toCurrency("1234.56", "EUR")  // locale-formatted EUR string
     *   FluentString.toCurrency("1234.56", "USD")  // locale-formatted USD string
     *   FluentString.toCurrency(null, "EUR")        // null
     * }</pre>
     *
     * @param s            the numeric string to format (may be {@code null})
     * @param currencyCode the ISO 4217 currency code (e.g. "EUR", "USD")
     * @return the formatted currency string, or {@code null} if s is null or unparseable
     */
    public static String toCurrency(String s, String currencyCode) {
        if (s == null) {
            return null;
        }
        try {
            double amount = Double.parseDouble(s.trim());
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat format = NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag(currencyToLocaleTag(currencyCode)));
            format.setCurrency(currency);
            return format.format(amount);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String currencyToLocaleTag(String currencyCode) {
        return switch (currencyCode.toUpperCase(Locale.ROOT)) {
            case "EUR" -> "fr-FR";
            case "USD" -> "en-US";
            case "GBP" -> "en-GB";
            case "JPY" -> "ja-JP";
            case "CHF" -> "de-CH";
            case "CAD" -> "en-CA";
            case "AUD" -> "en-AU";
            default -> "en-US";
        };
    }
}
