package io.fluentjava.optional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Fluent utility methods for {@link java.util.Optional}.
 *
 * <p>All methods are {@code public static}, null-safe, and side-effect free.
 * They are the compile-time rewrite targets for FluentJava's javac plugin.</p>
 *
 * <h3>Fluent usage (with javac plugin):</h3>
 * <pre>{@code
 *   Optional<User> opt = user.toOptional();
 *   String name = opt.mapTo(User::getName).orNull();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class FluentOptional {

    /** Utility class — cannot be instantiated. */
    private FluentOptional() {
        throw new AssertionError("FluentOptional is a utility class and cannot be instantiated");
    }

    /**
     * Wraps a value in an {@link Optional} using {@code Optional.ofNullable}.
     *
     * @param <T>   the value type
     * @param value the value (may be {@code null})
     * @return an Optional containing the value, or empty if null
     */
    public static <T> Optional<T> toOptional(T value) {
        return Optional.ofNullable(value);
    }

    /**
     * Returns the value inside the Optional, or {@code null} if empty.
     * Shorter than {@code opt.orElse(null)}.
     *
     * @param <T> the value type
     * @param opt the Optional (may be {@code null})
     * @return the value, or {@code null}
     */
    public static <T> T orNull(Optional<T> opt) {
        if (opt == null) {
            return null;
        }
        return opt.orElse(null);
    }

    /**
     * Returns the string value inside the Optional, or an empty string if absent.
     *
     * @param opt the Optional (may be {@code null})
     * @return the string value, or {@code ""}
     */
    public static String orEmpty(Optional<String> opt) {
        if (opt == null) {
            return "";
        }
        return opt.orElse("");
    }

    /**
     * Null-safe wrapper of {@link Optional#ifPresent(Consumer)}.
     *
     * @param <T>    the value type
     * @param opt    the Optional (may be {@code null})
     * @param action the action to execute if value is present
     */
    public static <T> void ifPresent(Optional<T> opt, Consumer<T> action) {
        if (opt != null) {
            opt.ifPresent(action);
        }
    }

    /**
     * Maps the Optional value using the function. Consistent naming with
     * {@code FluentList.mapTo}.
     *
     * @param <T> the source type
     * @param <R> the result type
     * @param opt the Optional (may be {@code null})
     * @param fn  the mapping function
     * @return a new Optional with the mapped value
     */
    public static <T, R> Optional<R> mapTo(Optional<T> opt, Function<T, R> fn) {
        if (opt == null) {
            return Optional.empty();
        }
        return opt.map(fn);
    }

    /**
     * Filters the Optional value by the predicate. Consistent naming with
     * {@code FluentList.filterBy}.
     *
     * @param <T>       the value type
     * @param opt       the Optional (may be {@code null})
     * @param predicate the filter predicate
     * @return the filtered Optional
     */
    public static <T> Optional<T> filterBy(Optional<T> opt, Predicate<T> predicate) {
        if (opt == null) {
            return Optional.empty();
        }
        return opt.filter(predicate);
    }

    /**
     * Null-safe check for {@link Optional#isPresent()}.
     *
     * @param <T> the value type
     * @param opt the Optional (may be {@code null})
     * @return {@code true} if the Optional is non-null and contains a value
     */
    public static <T> boolean isPresent(Optional<T> opt) {
        return opt != null && opt.isPresent();
    }

    /**
     * Null-safe check for {@link Optional#isEmpty()}.
     *
     * @param <T> the value type
     * @param opt the Optional (may be {@code null})
     * @return {@code true} if the Optional is null or empty
     */
    public static <T> boolean isEmpty(Optional<T> opt) {
        return opt == null || opt.isEmpty();
    }

    /**
     * Returns the value if present, otherwise throws an
     * {@link IllegalStateException} with the given message.
     *
     * @param <T>     the value type
     * @param opt     the Optional (may be {@code null})
     * @param message the error message when absent
     * @return the value
     * @throws IllegalStateException if the Optional is null or empty
     */
    public static <T> T orElseThrow(Optional<T> opt, String message) {
        if (opt == null || opt.isEmpty()) {
            throw new IllegalStateException(message);
        }
        return opt.get();
    }
}
