package io.fluentjava.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FluentDate exhaustive tests")
class FluentDateTest {

    @Test
    void weekendAndWeekday() {
        LocalDate saturday = LocalDate.of(2026, 3, 7);
        LocalDate monday = LocalDate.of(2026, 3, 9);

        assertTrue(FluentDate.isWeekend(saturday));
        assertFalse(FluentDate.isWeekend(monday));
        assertFalse(FluentDate.isWeekend(null));

        assertTrue(FluentDate.isWeekday(monday));
        assertFalse(FluentDate.isWeekday(saturday));
        assertFalse(FluentDate.isWeekday(null));
    }

    @Test
    void relativeToToday() {
        LocalDate today = LocalDate.now();
        assertTrue(FluentDate.isToday(today));
        assertFalse(FluentDate.isToday(today.minusDays(1)));
        assertFalse(FluentDate.isToday(null));

        assertTrue(FluentDate.isPast(today.minusDays(1)));
        assertFalse(FluentDate.isPast(today));
        assertFalse(FluentDate.isPast(null));

        assertTrue(FluentDate.isFuture(today.plusDays(1)));
        assertFalse(FluentDate.isFuture(today));
        assertFalse(FluentDate.isFuture(null));
    }

    @Test
    void leapAndDurations() {
        assertTrue(FluentDate.isLeapYear(LocalDate.of(2024, 2, 29)));
        assertFalse(FluentDate.isLeapYear(LocalDate.of(2025, 2, 28)));
        assertFalse(FluentDate.isLeapYear(null));

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 11);
        assertEquals(10L, FluentDate.daysUntil(from, to));
        assertEquals(0L, FluentDate.daysUntil(null, to));
        assertEquals(0L, FluentDate.daysUntil(from, null));

        assertEquals(2L, FluentDate.monthsUntil(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1)));
        assertEquals(0L, FluentDate.monthsUntil(null, to));
        assertEquals(0L, FluentDate.monthsUntil(from, null));

        LocalDate tenYearsAgo = LocalDate.now().minusYears(10);
        assertEquals(Period.between(tenYearsAgo, LocalDate.now()).getYears(), FluentDate.yearsUntilNow(tenYearsAgo));
        assertEquals(0, FluentDate.yearsUntilNow(null));
    }

    @Test
    void formatting() {
        LocalDate date = LocalDate.of(2026, 3, 6);
        LocalDateTime dateTime = LocalDateTime.of(2026, 3, 6, 10, 20, 30);

        assertEquals("2026-03-06", FluentDate.format(date, "yyyy-MM-dd"));
        assertEquals("2026-03-06 10:20", FluentDate.format(dateTime, "yyyy-MM-dd HH:mm"));

        assertNull(FluentDate.format((LocalDate) null, "yyyy-MM-dd"));
        assertNull(FluentDate.format(date, null));
        assertNull(FluentDate.format(date, "   "));

        assertNull(FluentDate.format((LocalDateTime) null, "yyyy-MM-dd"));
        assertNull(FluentDate.format(dateTime, ""));

        assertThrows(IllegalArgumentException.class, () -> FluentDate.format(date, "invalid pattern ["));
        assertThrows(IllegalArgumentException.class, () -> FluentDate.format(dateTime, "invalid pattern ["));
    }

    @Test
    void dayBoundariesAndEpoch() {
        LocalDate date = LocalDate.of(2026, 3, 6);
        LocalDateTime start = FluentDate.atStartOfDay(date);
        LocalDateTime end = FluentDate.atEndOfDay(date);

        assertEquals(LocalDateTime.of(2026, 3, 6, 0, 0, 0), start);
        assertEquals(LocalDateTime.of(2026, 3, 6, 23, 59, 59), end);
        assertNull(FluentDate.atStartOfDay(null));
        assertNull(FluentDate.atEndOfDay(null));

        LocalDateTime now = LocalDateTime.now().withNano(0);
        long millis = FluentDate.toEpochMillis(now);
        LocalDateTime restored = FluentDate.fromEpochMillis(millis);

        assertNotNull(restored);
        assertEquals(0L, FluentDate.toEpochMillis(null));
    }

    @Test
    void dateComparisons() {
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 1, 2);

        assertTrue(FluentDate.isBefore(d1, d2));
        assertFalse(FluentDate.isBefore(d2, d1));
        assertFalse(FluentDate.isBefore(null, d2));
        assertFalse(FluentDate.isBefore(d1, null));

        assertTrue(FluentDate.isAfter(d2, d1));
        assertFalse(FluentDate.isAfter(d1, d2));
        assertFalse(FluentDate.isAfter(null, d1));
        assertFalse(FluentDate.isAfter(d1, null));

        assertTrue(FluentDate.isBetween(LocalDate.of(2026, 1, 2), d1, LocalDate.of(2026, 1, 3)));
        assertTrue(FluentDate.isBetween(d1, d1, LocalDate.of(2026, 1, 3)));
        assertTrue(FluentDate.isBetween(LocalDate.of(2026, 1, 3), d1, LocalDate.of(2026, 1, 3)));
        assertFalse(FluentDate.isBetween(LocalDate.of(2025, 12, 31), d1, d2));
        assertFalse(FluentDate.isBetween(null, d1, d2));
        assertFalse(FluentDate.isBetween(d1, null, d2));
        assertFalse(FluentDate.isBetween(d1, d1, null));
    }

    @Test
    void dateTimeComparisons() {
        LocalDateTime dt1 = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime dt2 = LocalDateTime.of(2026, 1, 1, 11, 0);

        assertTrue(FluentDate.isBefore(dt1, dt2));
        assertFalse(FluentDate.isBefore(dt2, dt1));
        assertFalse(FluentDate.isBefore((LocalDateTime) null, dt2));
        assertFalse(FluentDate.isBefore(dt1, null));

        assertTrue(FluentDate.isAfter(dt2, dt1));
        assertFalse(FluentDate.isAfter(dt1, dt2));
        assertFalse(FluentDate.isAfter((LocalDateTime) null, dt1));
        assertFalse(FluentDate.isAfter(dt1, null));

        assertTrue(FluentDate.isBetween(LocalDateTime.of(2026, 1, 1, 10, 30), dt1, dt2));
        assertTrue(FluentDate.isBetween(dt1, dt1, dt2));
        assertTrue(FluentDate.isBetween(dt2, dt1, dt2));
        assertFalse(FluentDate.isBetween(LocalDateTime.of(2026, 1, 1, 9, 59), dt1, dt2));
        assertFalse(FluentDate.isBetween((LocalDateTime) null, dt1, dt2));
        assertFalse(FluentDate.isBetween(dt1, null, dt2));
        assertFalse(FluentDate.isBetween(dt1, dt1, null));
    }

    @Test
    void utilityClassConstructorThrows() throws Exception {
        var ctor = FluentDate.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(AssertionError.class, ex.getCause());
    }
}
