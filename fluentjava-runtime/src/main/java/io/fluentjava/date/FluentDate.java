package io.fluentjava.date;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

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

    // ────────────────────────────────────────────────────────────────
    // New methods
    // ────────────────────────────────────────────────────────────────

    /**
     * Returns the Monday of the week containing the given date (ISO week).
     *
     * @param date the date (may be {@code null})
     * @return the Monday of that week, or {@code null} if date is null
     */
    public static LocalDate startOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Returns the Sunday of the week containing the given date (ISO week).
     *
     * @param date the date (may be {@code null})
     * @return the Sunday of that week, or {@code null} if date is null
     */
    public static LocalDate endOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Returns the first day of the month for the given date.
     *
     * @param date the date (may be {@code null})
     * @return the first day of the month, or {@code null} if date is null
     */
    public static LocalDate startOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Returns the last day of the month for the given date.
     *
     * @param date the date (may be {@code null})
     * @return the last day of the month, or {@code null} if date is null
     */
    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Returns January 1st of the year containing the given date.
     *
     * @param date the date (may be {@code null})
     * @return the first day of the year, or {@code null} if date is null
     */
    public static LocalDate startOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * Returns December 31st of the year containing the given date.
     *
     * @param date the date (may be {@code null})
     * @return the last day of the year, or {@code null} if date is null
     */
    public static LocalDate endOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * Returns the next weekday (Monday–Friday). If the date is already a
     * weekday, returns the next one; if weekend, returns the following Monday.
     *
     * @param date the date (may be {@code null})
     * @return the next weekday, or {@code null} if date is null
     */
    public static LocalDate nextWeekday(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDate next = date.plusDays(1);
        while (isWeekend(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * Computes the age in whole years from the given birth date to today.
     * More readable alias for {@link #yearsUntilNow}.
     *
     * @param birthDate the birth date (may be {@code null})
     * @return the age in years, or 0 if null
     */
    public static int age(LocalDate birthDate) {
        return yearsUntilNow(birthDate);
    }

    /**
     * Checks whether the date is a business day (Monday–Friday).
     * More readable alias for {@link #isWeekday}.
     *
     * @param date the date (may be {@code null})
     * @return {@code true} if the date is a weekday
     */
    public static boolean isBusinessDay(LocalDate date) {
        return isWeekday(date);
    }

    /**
     * Parses a string into a {@link LocalDate} using the given pattern.
     * Returns {@code null} if parsing fails or input is null.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentDate.toLocalDate("25/12/2024", "dd/MM/yyyy")  // 2024-12-25
     *   FluentDate.toLocalDate("invalid", "dd/MM/yyyy")      // null
     *   FluentDate.toLocalDate(null, "dd/MM/yyyy")            // null
     * }</pre>
     *
     * @param s       the string to parse (may be {@code null})
     * @param pattern the date pattern (may be {@code null})
     * @return the parsed {@link LocalDate}, or {@code null} on failure
     */
    public static LocalDate toLocalDate(String s, String pattern) {
        if (isBlank(s) || isBlank(pattern)) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a string into a {@link LocalDateTime} using the given pattern.
     * Returns {@code null} if parsing fails or input is null.
     *
     * @param s       the string to parse (may be {@code null})
     * @param pattern the datetime pattern (may be {@code null})
     * @return the parsed {@link LocalDateTime}, or {@code null} on failure
     */
    public static LocalDateTime toLocalDateTime(String s, String pattern) {
        if (isBlank(s) || isBlank(pattern)) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.trim(), DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Returns the quarter (1–4) of the year for the given date.
     *
     * @param date the date (may be {@code null})
     * @return the quarter number (1–4), or 0 if null
     */
    public static int quarterOf(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * Returns the ISO week-of-year number for the given date.
     *
     * @param date the date (may be {@code null})
     * @return the ISO week number, or 0 if null
     */
    public static int weekOfYear(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }
}
