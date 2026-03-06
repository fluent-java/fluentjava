package io.fluentjava.validator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FluentValidatorTest {

    @Nested
    class NotNull {

        @Test
        void nullValueReturnsError() {
            List<String> errors = FluentValidator.of(null)
                    .notNull("required")
                    .validate();
            assertEquals(List.of("required"), errors);
        }

        @Test
        void nonNullValueNoError() {
            assertTrue(FluentValidator.of("hello")
                    .notNull("required")
                    .isValid());
        }
    }

    @Nested
    class StringRules {

        @Test
        void minLengthTooShortReturnsError() {
            List<String> errors = FluentValidator.of("ab")
                    .minLength(3, "too short")
                    .validate();
            assertEquals(List.of("too short"), errors);
        }

        @Test
        void minLengthNullInputIsSkipped() {
            assertTrue(FluentValidator.<String>of(null)
                    .minLength(3, "too short")
                    .isValid());
        }

        @Test
        void minLengthExactLengthNoError() {
            assertTrue(FluentValidator.of("abc")
                    .minLength(3, "too short")
                    .isValid());
        }

        @Test
        void notBlankAndMaxLengthAndRegex() {
            List<String> errors = FluentValidator.of("")
                    .notBlank("blank")
                    .maxLength(0, "too long")
                    .matches("[A-Z]+", "invalid format")
                    .validate();

            assertEquals(List.of("blank", "invalid format"), errors);
        }

        @Test
        void ofStringFactoryWorks() {
            assertTrue(FluentValidator.ofString("john")
                    .notBlank("blank")
                    .minLength(3, "too short")
                    .isValid());
        }
    }

    @Nested
    class NotEmptyRule {

        @Test
        void notEmptyForStringCollectionMap() {
            assertEquals(List.of("empty"), FluentValidator.of("").notEmpty("empty").validate());
            assertEquals(List.of("empty"), FluentValidator.of(List.of()).notEmpty("empty").validate());
            assertEquals(List.of("empty"), FluentValidator.of(Map.of()).notEmpty("empty").validate());

            assertTrue(FluentValidator.of("x").notEmpty("empty").isValid());
            assertTrue(FluentValidator.of(List.of(1)).notEmpty("empty").isValid());
            assertTrue(FluentValidator.of(Map.of("k", "v")).notEmpty("empty").isValid());
        }

        @Test
        void notEmptyNullAndUnsupportedTypeSkipped() {
            assertTrue(FluentValidator.of((Object) null).notEmpty("empty").isValid());
            assertTrue(FluentValidator.of(123).notEmpty("empty").isValid());
        }
    }

    @Nested
    class NumberRules {

        @Test
        void minMaxPositive() {
            List<String> errors = FluentValidator.of(-5)
                    .min(0, "min")
                    .max(10, "max")
                    .positive("positive")
                    .validate();

            assertEquals(List.of("min", "positive"), errors);
        }

        @Test
        void numberRulesSkipNullAndNonNumber() {
            assertTrue(FluentValidator.of((Integer) null).min(0, "min").max(1, "max").positive("p").isValid());
            assertTrue(FluentValidator.of("abc").min(0, "min").max(1, "max").positive("p").isValid());
        }
    }

    @Nested
    class MultipleRules {

        @Test
        void allErrorsReturned() {
            List<String> errors = FluentValidator.of("")
                    .notBlank("blank")
                    .minLength(3, "short")
                    .validate();

            assertEquals(2, errors.size());
            assertEquals(List.of("blank", "short"), errors);
        }

        @Test
        void firstErrorReturnsFirstFailureInOrder() {
            String error = FluentValidator.of((String) null)
                    .notNull("null")
                    .notBlank("blank")
                    .firstError();

            assertEquals("null", error);
        }

        @Test
        void firstErrorReturnsNullWhenNoError() {
            assertNull(FluentValidator.of("ok").notNull("required").firstError());
        }
    }

    @Nested
    class ThrowIfInvalid {

        @Test
        void invalidThrowsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () ->
                    FluentValidator.of((String) null)
                            .notNull("required")
                            .throwIfInvalid());
        }

        @Test
        void validDoesNotThrow() {
            assertDoesNotThrow(() ->
                    FluentValidator.of("ok")
                            .notNull("required")
                            .throwIfInvalid());
        }

        @Test
        void customExceptionFactoryIsUsed() {
            RuntimeException ex = assertThrows(IllegalStateException.class, () ->
                    FluentValidator.of("")
                            .notBlank("blank")
                            .throwIfInvalid(errors -> new IllegalStateException(String.join(" | ", errors))));
            assertEquals("blank", ex.getMessage());
        }
    }

    @Nested
    class Satisfies {

        @Test
        void satisfiesWorksForValidAndInvalidCases() {
            assertTrue(FluentValidator.of("Abc123")
                    .satisfies(v -> v.length() >= 6, "len")
                    .satisfies(v -> v.matches(".*[A-Z].*"), "upper")
                    .isValid());

            List<String> errors = FluentValidator.of("abc")
                    .satisfies(v -> v.length() >= 6, "len")
                    .satisfies(v -> v.matches(".*[A-Z].*"), "upper")
                    .validate();

            assertEquals(List.of("len", "upper"), errors);
        }

        @Test
        void satisfiesSkipsNull() {
            assertTrue(FluentValidator.<String>of(null)
                    .satisfies(v -> !v.isEmpty(), "x")
                    .isValid());
        }
    }
}
