package io.fluentjava.string;

import java.math.BigDecimal;
import java.text.Normalizer;
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
}
