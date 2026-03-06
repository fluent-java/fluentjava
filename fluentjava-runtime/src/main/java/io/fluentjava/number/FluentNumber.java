package io.fluentjava.number;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
}
