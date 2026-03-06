package io.fluentjava.object;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class FluentObject {

    private FluentObject() {
        throw new AssertionError("FluentObject is a utility class and cannot be instantiated");
    }

    /**
     * Performs the given action on the object and returns the object itself.
     *
     * @param obj the object to operate on (may be {@code null})
     * @param action the action to perform (must not be {@code null})
     * @return the original object
     */
    public static <T> T also(T obj, Consumer<? super T> action) {
        action.accept(obj);
        return obj;
    }

    /**
     * Applies the given function to the object and returns the result.
     *
     * @param obj the object to operate on (may be {@code null})
     * @param fn the function to apply (must not be {@code null})
     * @return the result of applying the function
     */
    public static <T, R> R let(T obj, Function<? super T, ? extends R> fn) {
        return fn.apply(obj);
    }

    /**
     * Returns the object if it matches the predicate, otherwise returns null.
     *
     * @param obj the object to test (may be {@code null})
     * @param pred the predicate to test (must not be {@code null})
     * @return the object if predicate is true, otherwise null
     */
    public static <T> T takeIf(T obj, Predicate<? super T> pred) {
        if (obj == null) {
            return null;
        }
        return pred.test(obj) ? obj : null;
    }

    /**
     * Returns the object if it does not match the predicate, otherwise returns null.
     *
     * @param obj the object to test (may be {@code null})
     * @param pred the predicate to test (must not be {@code null})
     * @return the object if predicate is false, otherwise null
     */
    public static <T> T takeUnless(T obj, Predicate<? super T> pred) {
        if (obj == null) {
            return null;
        }
        return pred.test(obj) ? null : obj;
    }

    /**
     * Checks if the object is null.
     *
     * @param obj the object to check
     * @return {@code true} if the object is null
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Checks if the object is not null.
     *
     * @param obj the object to check
     * @return {@code true} if the object is not null
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * Returns the object if not null, otherwise returns the fallback value.
     *
     * @param obj the object to check (may be {@code null})
     * @param fallback the value to return if obj is null
     * @return obj if not null, otherwise fallback
     */
    public static <T> T orElse(T obj, T fallback) {
        return obj == null ? fallback : obj;
    }

    /**
     * Returns the object if not null, otherwise returns the value from the supplier.
     *
     * @param obj the object to check (may be {@code null})
     * @param supplier the supplier to provide a value if obj is null
     * @return obj if not null, otherwise supplier.get()
     */
    public static <T> T orElseGet(T obj, Supplier<? extends T> supplier) {
        return obj == null ? supplier.get() : obj;
    }

    /**
     * Returns the object if not null, otherwise throws IllegalArgumentException with the given message.
     *
     * @param obj the object to check (may be {@code null})
     * @param message the exception message if obj is null (may be {@code null})
     * @return obj if not null
     * @throws IllegalArgumentException if obj is null
     */
    public static <T> T requireNotNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message == null ? "value must not be null" : message);
        }
        return obj;
    }

    /**
     * Throws IllegalArgumentException with the given message if the condition is false.
     *
     * @param condition the condition to check
     * @param message the exception message if condition is false (may be {@code null})
     * @throws IllegalArgumentException if condition is false
     */
    public static void requireThat(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message == null ? "requirement failed" : message);
        }
    }
}
