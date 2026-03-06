package io.fluentjava.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Fluent builder for value validation.
 *
 * <p>Each validation method appends a rule to an internal list and returns
 * {@code this} for method chaining. Rules are represented as
 * {@code Supplier<Optional<String>>}, where:</p>
 * <ul>
 *   <li>{@code Optional.empty()} means the rule passed</li>
 *   <li>{@code Optional.of(message)} means the rule failed</li>
 * </ul>
 *
 * <p>The builder is null-safe by design: rules that cannot execute on
 * {@code null} values are skipped, while {@link #notNull(String)} is the
 * rule dedicated to null detection.</p>
 *
 * @param <T> the validated value type
 * @since 1.0.0
 */
public final class FluentValidator<T> {

    private final T value;
    private final List<Supplier<Optional<String>>> rules = new ArrayList<>();

    private FluentValidator(T value) {
        this.value = value;
    }

    /**
     * Creates a validator for the given value.
     *
     * @param <T> the value type
     * @param value the value to validate (may be {@code null})
     * @return a new validator instance
     */
    public static <T> FluentValidator<T> of(T value) {
        return new FluentValidator<>(value);
    }

    /**
     * Creates a validator specialized for string values.
     *
     * @param value the string value to validate (may be {@code null})
     * @return a new string validator instance
     */
    public static FluentValidator<String> ofString(String value) {
        return new FluentValidator<>(value);
    }

    /**
     * Appends a rule to this validator and returns the same instance.
     *
     * @param rule the validation rule supplier
     * @return this validator for chaining
     */
    private FluentValidator<T> addRule(Supplier<Optional<String>> rule) {
        rules.add(rule);
        return this;
    }

    /**
     * Adds a rule that fails when the value is {@code null}.
     *
     * @param message the error message returned when value is null
     * @return this validator for chaining
     */
    public FluentValidator<T> notNull(String message) {
        return addRule(() -> value == null ? Optional.of(message) : Optional.empty());
    }

    /**
     * Adds a custom predicate rule for the current value.
     *
     * @param condition the predicate that must return {@code true}
     * @param message the error message returned when predicate fails
     * @return this validator for chaining
     */
    public FluentValidator<T> satisfies(Predicate<T> condition, String message) {
        return addRule(() -> {
            if (value == null) {
                return Optional.empty();
            }
            return condition.test(value) ? Optional.empty() : Optional.of(message);
        });
    }

    /**
     * Adds a string rule that fails when the value is blank.
     *
     * @param message the error message returned when blank
     * @return this validator for chaining
     */
    public FluentValidator<T> notBlank(String message) {
        return addRule(() -> {
            if (!(value instanceof String stringValue)) {
                return Optional.empty();
            }
            return stringValue.isBlank() ? Optional.of(message) : Optional.empty();
        });
    }

    /**
     * Adds a string rule that fails when length is lower than {@code min}.
     *
     * @param min the minimum accepted length
     * @param message the error message returned when too short
     * @return this validator for chaining
     */
    public FluentValidator<T> minLength(int min, String message) {
        return addRule(() -> {
            if (!(value instanceof String stringValue)) {
                return Optional.empty();
            }
            return stringValue.length() < min ? Optional.of(message) : Optional.empty();
        });
    }

    /**
     * Adds a string rule that fails when length is greater than {@code max}.
     *
     * @param max the maximum accepted length
     * @param message the error message returned when too long
     * @return this validator for chaining
     */
    public FluentValidator<T> maxLength(int max, String message) {
        return addRule(() -> {
            if (!(value instanceof String stringValue)) {
                return Optional.empty();
            }
            return stringValue.length() > max ? Optional.of(message) : Optional.empty();
        });
    }

    /**
     * Adds a string rule that fails when regex does not match.
     *
     * @param regex the regular expression to satisfy
     * @param message the error message returned when regex does not match
     * @return this validator for chaining
     */
    public FluentValidator<T> matches(String regex, String message) {
        return addRule(() -> {
            if (!(value instanceof String stringValue)) {
                return Optional.empty();
            }
            return Pattern.matches(regex, stringValue) ? Optional.empty() : Optional.of(message);
        });
    }

    /**
     * Adds a rule that fails when supported value types are empty.
     *
     * <p>Supported types: {@link String}, {@link Collection}, {@link Map}.</p>
     *
     * @param message the error message returned when empty
     * @return this validator for chaining
     */
    public FluentValidator<T> notEmpty(String message) {
        return addRule(() -> {
            if (value instanceof String stringValue) {
                return stringValue.isEmpty() ? Optional.of(message) : Optional.empty();
            }
            if (value instanceof Collection<?> collectionValue) {
                return collectionValue.isEmpty() ? Optional.of(message) : Optional.empty();
            }
            if (value instanceof Map<?, ?> mapValue) {
                return mapValue.isEmpty() ? Optional.of(message) : Optional.empty();
            }
            return Optional.empty();
        });
    }

    /**
     * Adds a numeric rule that fails when value is less than {@code minValue}.
     *
     * @param minValue the minimum numeric value
     * @param message the error message returned when lower than minimum
     * @return this validator for chaining
     */
    public FluentValidator<T> min(double minValue, String message) {
        return addRule(() -> {
            if (!(value instanceof Number numberValue)) {
                return Optional.empty();
            }
            return numberValue.doubleValue() < minValue ? Optional.of(message) : Optional.empty();
        });
    }

    /**
     * Adds a numeric rule that fails when value is greater than {@code maxValue}.
     *
     * @param maxValue the maximum numeric value
     * @param message the error message returned when greater than maximum
     * @return this validator for chaining
     */
    public FluentValidator<T> max(double maxValue, String message) {
        return addRule(() -> {
            if (!(value instanceof Number numberValue)) {
                return Optional.empty();
            }
            return numberValue.doubleValue() > maxValue ? Optional.of(message) : Optional.empty();
        });
    }

    /**
     * Adds a numeric rule that fails when value is not strictly positive.
     *
     * @param message the error message returned when value is zero or negative
     * @return this validator for chaining
     */
    public FluentValidator<T> positive(String message) {
        return addRule(() -> {
            if (!(value instanceof Number numberValue)) {
                return Optional.empty();
            }
            return numberValue.doubleValue() <= 0d ? Optional.of(message) : Optional.empty();
        });
    }

    /**
     * Executes all rules and returns all error messages in insertion order.
     *
     * @return a list of validation error messages
     */
    public List<String> validate() {
        return rules.stream()
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Returns whether all configured rules pass.
     *
     * @return {@code true} when there are no validation errors
     */
    public boolean isValid() {
        return validate().isEmpty();
    }

    /**
     * Throws {@link IllegalArgumentException} when validation fails.
     *
     * @throws IllegalArgumentException when at least one rule fails
     */
    public void throwIfInvalid() {
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    /**
     * Throws a custom exception built from the current error list.
     *
     * @param exceptionFn factory that maps error list to an exception
     * @throws RuntimeException the custom exception produced by {@code exceptionFn}
     */
    public void throwIfInvalid(Function<List<String>, RuntimeException> exceptionFn) {
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw exceptionFn.apply(errors);
        }
    }

    /**
     * Returns the first error message, or {@code null} if none.
     *
     * @return the first validation error message, or {@code null}
     */
    public String firstError() {
        return rules.stream()
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }
}
