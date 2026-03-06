package io.fluentjava.date;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class FluentDate {

    private FluentDate() {
        throw new AssertionError("FluentDate is a utility class and cannot be instantiated");
    }

    /**
     * Checks whether a date falls on Saturday or Sunday.
     *
     * @param date the date to evaluate (may be {@code null})
     * @return {@code true} when the date is Saturday or Sunday
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek d = date.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    /**
     * Checks whether a date is a weekday.
     *
     * @param date the date to evaluate (may be {@code null})
     * @return {@code true} when the date is Monday to Friday
     */
    public static boolean isWeekday(LocalDate date) {
        return date != null && !isWeekend(date);
    }

    /**
     * Checks whether a date is equal to today's local date.
     *
     * @param date the date to evaluate (may be {@code null})
     * @return {@code true} when the date is today
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.isEqual(LocalDate.now());
    }

    /**
     * Checks whether a date is before today.
     *
     * @param date the date to evaluate (may be {@code null})
     * @return {@code true} when the date is in the past
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks whether a date is after today.
     *
     * @param date the date to evaluate (may be {@code null})
     * @return {@code true} when the date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Checks whether the year of a date is a leap year.
     *
     * @param date the date to evaluate (may be {@code null})
     * @return {@code true} when the date year is leap
     */
    public static boolean isLeapYear(LocalDate date) {
        return date != null && date.isLeapYear();
    }

    /**
     * Computes the number of days between two dates.
     *
     * @param from the start date (may be {@code null})
     * @param to the end date (may be {@code null})
     * @return the number of days between {@code from} and {@code to}, or {@code 0} when either is {@code null}
     */
    public static long daysUntil(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(from, to);
    }

    /**
     * Computes the number of whole months between two dates.
     *
     * @param from the start date (may be {@code null})
     * @param to the end date (may be {@code null})
     * @return the number of months between {@code from} and {@code to}, or {@code 0} when either is {@code null}
     */
    public static long monthsUntil(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return 0L;
        }
        return ChronoUnit.MONTHS.between(from, to);
    }

    /**
     * Computes the whole number of years between a date and now.
     *
     * @param date the start date (may be {@code null})
     * @return elapsed whole years, or {@code 0} when date is {@code null}
     */
    public static int yearsUntilNow(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return Period.between(date, LocalDate.now()).getYears();
    }

    /**
     * Formats a {@link LocalDate} with the given pattern.
     *
     * @param date the date to format (may be {@code null})
     * @param pattern the formatter pattern (may be {@code null} or blank)
     * @return the formatted date string, or {@code null} when inputs are invalid
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || isBlank(pattern)) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats a {@link LocalDateTime} with the given pattern.
     *
     * @param dateTime the datetime to format (may be {@code null})
     * @param pattern the formatter pattern (may be {@code null} or blank)
     * @return the formatted datetime string, or {@code null} when inputs are invalid
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || isBlank(pattern)) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Creates a datetime at midnight for the given date.
     *
     * @param date the source date (may be {@code null})
     * @return the datetime at 00:00:00, or {@code null} when date is {@code null}
     */
    public static LocalDateTime atStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Creates a datetime at the last second of the day for the given date.
     *
     * @param date the source date (may be {@code null})
     * @return the datetime at 23:59:59, or {@code null} when date is {@code null}
     */
    public static LocalDateTime atEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }

    /**
     * Converts a local datetime to epoch milliseconds in the system default zone.
     *
     * @param dateTime the datetime to convert (may be {@code null})
     * @return epoch milliseconds, or {@code 0L} when datetime is {@code null}
     */
    public static long toEpochMillis(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Converts epoch milliseconds to a local datetime in the system default zone.
     *
     * @param millis the epoch milliseconds
     * @return the converted local datetime
     */
    public static LocalDateTime fromEpochMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    /**
     * Checks whether one date is strictly before another date.
     *
     * @param date the candidate date (may be {@code null})
     * @param other the reference date (may be {@code null})
     * @return {@code true} when {@code date} is before {@code other}
     */
    public static boolean isBefore(LocalDate date, LocalDate other) {
        return date != null && other != null && date.isBefore(other);
    }

    /**
     * Checks whether one date is strictly after another date.
     *
     * @param date the candidate date (may be {@code null})
     * @param other the reference date (may be {@code null})
     * @return {@code true} when {@code date} is after {@code other}
     */
    public static boolean isAfter(LocalDate date, LocalDate other) {
        return date != null && other != null && date.isAfter(other);
    }

    /**
     * Checks whether a date is inside an inclusive date range.
     *
     * @param date the candidate date (may be {@code null})
     * @param from the range start (inclusive, may be {@code null})
     * @param to the range end (inclusive, may be {@code null})
     * @return {@code true} when date is within [{@code from}, {@code to}]
     */
    public static boolean isBetween(LocalDate date, LocalDate from, LocalDate to) {
        return date != null && from != null && to != null && !date.isBefore(from) && !date.isAfter(to);
    }

    /**
     * Checks whether one datetime is strictly before another datetime.
     *
     * @param date the candidate datetime (may be {@code null})
     * @param other the reference datetime (may be {@code null})
     * @return {@code true} when {@code date} is before {@code other}
     */
    public static boolean isBefore(LocalDateTime date, LocalDateTime other) {
        return date != null && other != null && date.isBefore(other);
    }

    /**
     * Checks whether one datetime is strictly after another datetime.
     *
     * @param date the candidate datetime (may be {@code null})
     * @param other the reference datetime (may be {@code null})
     * @return {@code true} when {@code date} is after {@code other}
     */
    public static boolean isAfter(LocalDateTime date, LocalDateTime other) {
        return date != null && other != null && date.isAfter(other);
    }

    /**
     * Checks whether a datetime is inside an inclusive datetime range.
     *
     * @param date the candidate datetime (may be {@code null})
     * @param from the range start (inclusive, may be {@code null})
     * @param to the range end (inclusive, may be {@code null})
     * @return {@code true} when datetime is within [{@code from}, {@code to}]
     */
    public static boolean isBetween(LocalDateTime date, LocalDateTime from, LocalDateTime to) {
        return date != null && from != null && to != null && !date.isBefore(from) && !date.isAfter(to);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
