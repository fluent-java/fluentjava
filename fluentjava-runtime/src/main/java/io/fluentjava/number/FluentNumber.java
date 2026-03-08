package io.fluentjava.number;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FluentNumber {

    private FluentNumber() {
        throw new AssertionError("FluentNumber is a utility class and cannot be instantiated");
    }

    /**
     * Clamps the value to the given range [min, max].
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return value if in range, otherwise min or max
     */
    public static double coerceIn(double value, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Returns the value if it is at least min, otherwise returns min.
     *
     * @param value the value to check
     * @param min the minimum allowed value
     * @return value if >= min, otherwise min
     */
    public static double coerceAtLeast(double value, double min) {
        return Math.max(value, min);
    }

    /**
     * Returns the value if it is at most max, otherwise returns max.
     *
     * @param value the value to check
     * @param max the maximum allowed value
     * @return value if <= max, otherwise max
     */
    public static double coerceAtMost(double value, double max) {
        return Math.min(value, max);
    }

    /**
     * Checks if the value is between min and max (inclusive).
     *
     * @param value the value to check
     * @param min the minimum value
     * @param max the maximum value
     * @return {@code true} if value is in [min, max]
     */
    public static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Checks if the number is positive (> 0).
     *
     * @param n the number to check (may be {@code null})
     * @return {@code true} if n is positive
     */
    public static boolean isPositive(Number n) {
        return n != null && n.doubleValue() > 0d;
    }

    /**
     * Checks if the number is negative (< 0).
     *
     * @param n the number to check (may be {@code null})
     * @return {@code true} if n is negative
     */
    public static boolean isNegative(Number n) {
        return n != null && n.doubleValue() < 0d;
    }

    /**
     * Checks if the number is zero.
     *
     * @param n the number to check (may be {@code null})
     * @return {@code true} if n is zero
     */
    public static boolean isZero(Number n) {
        return n != null && n.doubleValue() == 0d;
    }

    /**
     * Rounds the value to the given number of decimal places (HALF_UP).
     *
     * @param value the value to round
     * @param decimals the number of decimal places (>= 0)
     * @return the rounded value
     */
    public static double roundTo(double value, int decimals) {
        if (decimals < 0) {
            throw new IllegalArgumentException("decimals must be >= 0");
        }
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Returns the percentage that part is of total (0 if total is 0).
     *
     * @param part the part value
     * @param total the total value
     * @return the percentage (0 if total is 0)
     */
    public static double percentOf(double part, double total) {
        if (total == 0d) {
            return 0d;
        }
        return (part / total) * 100d;
    }

    /**
     * Checks if the number is even.
     *
     * @param n the number to check
     * @return {@code true} if n is even
     */
    public static boolean isEven(long n) {
        return (n & 1L) == 0L;
    }

    /**
     * Checks if the number is odd.
     *
     * @param n the number to check
     * @return {@code true} if n is odd
     */
    public static boolean isOdd(long n) {
        return (n & 1L) != 0L;
    }

    /**
     * Converts an integer to its English ordinal representation.
     *
     * @param n the number to convert
     * @return the ordinal representation (for example, {@code 1st}, {@code 2nd}, {@code 11th})
     */
    public static String toOrdinal(int n) {
        int abs = Math.abs(n);
        int mod100 = abs % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return n + "th";
        }
        return switch (abs % 10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }

    // ────────────────────────────────────────────────────────────────
    // New methods
    // ────────────────────────────────────────────────────────────────

    /**
     * Formats a number as a percentage string with the given decimal places.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentNumber.toPercentString(0.75, 2)   // "75.00%"
     *   FluentNumber.toPercentString(0.5, 0)    // "50%"
     *   FluentNumber.toPercentString(null, 2)   // null
     * }</pre>
     *
     * @param n        the number to format (may be {@code null})
     * @param decimals the number of decimal places (>= 0)
     * @return the formatted percentage string, or {@code null} if n is null
     */
    public static String toPercentString(Number n, int decimals) {
        if (n == null) {
            return null;
        }
        return String.format("%." + Math.max(0, decimals) + "f%%", n.doubleValue() * 100);
    }

    /**
     * Clamps the value to the given range [min, max]. Alias of {@link #coerceIn}.
     *
     * @param n   the number to clamp (may be {@code null})
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return clamped value, or 0 if n is null
     */
    public static double clamp(Number n, Number min, Number max) {
        if (n == null) {
            return 0d;
        }
        return coerceIn(n.doubleValue(), min.doubleValue(), max.doubleValue());
    }

    /**
     * Checks if the number is a prime number.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentNumber.isPrime(7)   // true
     *   FluentNumber.isPrime(4)   // false
     *   FluentNumber.isPrime(1)   // false
     * }</pre>
     *
     * @param n the number to check
     * @return {@code true} if n is prime
     */
    public static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        if (n < 4) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }
        for (int i = 5; (long) i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes the factorial of the given number.
     * Returns -1 for negative numbers.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentNumber.factorial(5)   // 120
     *   FluentNumber.factorial(0)   // 1
     *   FluentNumber.factorial(-1)  // -1
     * }</pre>
     *
     * @param n the number (0! = 1)
     * @return the factorial, or -1 if negative
     */
    public static long factorial(int n) {
        if (n < 0) {
            return -1L;
        }
        long result = 1L;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Returns the digits of the absolute value of the number as a list.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentNumber.digits(123)  // [1, 2, 3]
     *   FluentNumber.digits(0)    // [0]
     *   FluentNumber.digits(-42)  // [4, 2]
     * }</pre>
     *
     * @param n the number
     * @return an unmodifiable list of digits
     */
    public static List<Integer> digits(int n) {
        int abs = Math.abs(n);
        if (abs == 0) {
            return List.of(0);
        }
        List<Integer> out = new ArrayList<>();
        while (abs > 0) {
            out.add(abs % 10);
            abs /= 10;
        }
        Collections.reverse(out);
        return List.copyOf(out);
    }

    /**
     * Returns the binary string representation of the number.
     *
     * @param n the number
     * @return the binary string (e.g. "1010")
     */
    public static String toBinary(int n) {
        return Integer.toBinaryString(n);
    }

    /**
     * Returns the hexadecimal string representation of the number (lowercase).
     *
     * @param n the number
     * @return the hex string (e.g. "ff")
     */
    public static String toHex(int n) {
        return Integer.toHexString(n);
    }

    /**
     * Returns the octal string representation of the number.
     *
     * @param n the number
     * @return the octal string (e.g. "17")
     */
    public static String toOctal(int n) {
        return Integer.toOctalString(n);
    }
}
