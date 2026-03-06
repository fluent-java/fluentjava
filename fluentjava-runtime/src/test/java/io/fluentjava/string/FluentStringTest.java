package io.fluentjava.string;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link FluentString}.
 * Covers null, empty, blank, valid, invalid, and edge cases for every method.
 */
@DisplayName("FluentString")
class FluentStringTest {

    // ═════════════════════════════════════════════════════════════════════════
    // isBlankSafe
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isBlankSafe()")
    class IsBlankSafe {

        @Test
        @DisplayName("null → true")
        void nullInput() {
            assertTrue(FluentString.isBlankSafe(null));
        }

        @Test
        @DisplayName("\"\" → true")
        void emptyInput() {
            assertTrue(FluentString.isBlankSafe(""));
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n", "\r\n", " \t\n "})
        @DisplayName("whitespace-only → true")
        void blankInputs(String input) {
            assertTrue(FluentString.isBlankSafe(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "hello", " hi ", "123", "0"})
        @DisplayName("non-blank → false")
        void nonBlankInputs(String input) {
            assertFalse(FluentString.isBlankSafe(input));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // trimToNull
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("trimToNull()")
    class TrimToNull {

        @Test
        @DisplayName("null → null")
        void nullInput() {
            assertNull(FluentString.trimToNull(null));
        }

        @Test
        @DisplayName("\"\" → null")
        void emptyInput() {
            assertNull(FluentString.trimToNull(""));
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n", " \t\n "})
        @DisplayName("blank → null")
        void blankInputs(String input) {
            assertNull(FluentString.trimToNull(input));
        }

        @Test
        @DisplayName("\"  hello  \" → \"hello\"")
        void trims() {
            assertEquals("hello", FluentString.trimToNull("  hello  "));
        }

        @Test
        @DisplayName("\"hello\" → \"hello\" (no trim needed)")
        void noTrimNeeded() {
            assertEquals("hello", FluentString.trimToNull("hello"));
        }

        @Test
        @DisplayName("preserves internal spaces")
        void internalSpaces() {
            assertEquals("hello world", FluentString.trimToNull("  hello world  "));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // toIntOrNull
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toIntOrNull()")
    class ToIntOrNull {

        @Test
        @DisplayName("\"42\" → 42")
        void validPositive() {
            assertEquals(42, FluentString.toIntOrNull("42"));
        }

        @Test
        @DisplayName("\"-7\" → -7")
        void validNegative() {
            assertEquals(-7, FluentString.toIntOrNull("-7"));
        }

        @Test
        @DisplayName("\"0\" → 0")
        void zero() {
            assertEquals(0, FluentString.toIntOrNull("0"));
        }

        @Test
        @DisplayName("\"  42  \" → 42 (trimmed)")
        void trimmedParsing() {
            assertEquals(42, FluentString.toIntOrNull("  42  "));
        }

        @Test
        @DisplayName("Integer.MAX_VALUE parses correctly")
        void maxValue() {
            assertEquals(Integer.MAX_VALUE,
                    FluentString.toIntOrNull(String.valueOf(Integer.MAX_VALUE)));
        }

        @Test
        @DisplayName("Integer.MIN_VALUE parses correctly")
        void minValue() {
            assertEquals(Integer.MIN_VALUE,
                    FluentString.toIntOrNull(String.valueOf(Integer.MIN_VALUE)));
        }

        @Test
        @DisplayName("null → null")
        void nullInput() {
            assertNull(FluentString.toIntOrNull(null));
        }

        @Test
        @DisplayName("\"\" → null")
        void emptyInput() {
            assertNull(FluentString.toIntOrNull(""));
        }

        @Test
        @DisplayName("\"abc\" → null")
        void invalidLetters() {
            assertNull(FluentString.toIntOrNull("abc"));
        }

        @Test
        @DisplayName("\"3.14\" → null (decimal)")
        void decimalRejected() {
            assertNull(FluentString.toIntOrNull("3.14"));
        }

        @Test
        @DisplayName("overflow → null")
        void overflow() {
            assertNull(FluentString.toIntOrNull("99999999999999999999"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // mask
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("mask()")
    class Mask {

        @Test
        @DisplayName("\"1234567890\" mask(4) → \"******7890\"")
        void standardMask() {
            assertEquals("******7890", FluentString.mask("1234567890", 4));
        }

        @Test
        @DisplayName("visible 0 → all masked")
        void maskAll() {
            assertEquals("***", FluentString.mask("abc", 0));
        }

        @Test
        @DisplayName("visible >= length → original returned")
        void nothingToMask() {
            assertEquals("abc", FluentString.mask("abc", 5));
            assertEquals("abc", FluentString.mask("abc", 3));
        }

        @Test
        @DisplayName("null → null")
        void nullInput() {
            assertNull(FluentString.mask(null, 3));
        }

        @Test
        @DisplayName("\"\" → \"\"")
        void emptyInput() {
            assertEquals("", FluentString.mask("", 3));
        }

        @Test
        @DisplayName("negative visible → IllegalArgumentException")
        void negativeVisible() {
            assertThrows(IllegalArgumentException.class,
                    () -> FluentString.mask("abc", -1));
        }

        @Test
        @DisplayName("single char, visible 0")
        void singleCharMaskAll() {
            assertEquals("*", FluentString.mask("X", 0));
        }

        @Test
        @DisplayName("single char, visible 1")
        void singleCharVisible() {
            assertEquals("X", FluentString.mask("X", 1));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Utility class contract
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("constructor throws AssertionError (utility class)")
    void cannotInstantiate() throws Exception {
        var ctor = FluentString.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        var ex = assertThrows(java.lang.reflect.InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(AssertionError.class, ex.getCause());
    }
}
