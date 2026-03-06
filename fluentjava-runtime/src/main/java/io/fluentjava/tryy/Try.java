package io.fluentjava.tryy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Try<T> {

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Throwable;
    }

    private final T value;
    private final Throwable error;

    private Try(T value, Throwable error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Fluent helper method.
     */
    public static <T> Try<T> of(CheckedSupplier<? extends T> supplier) {
        try {
            return new Try<>(supplier.get(), null);
        } catch (Throwable t) {
            return new Try<>(null, t);
        }
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T orNull() {
        return value;
    }

    public T orElse(T fallback) {
        return isSuccess() ? value : fallback;
    }

    public T orElseGet(Supplier<? extends T> fallback) {
        return isSuccess() ? value : fallback.get();
    }

    public T orElseThrow() {
        if (isSuccess()) {
            return value;
        }
        if (error instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (error instanceof Error severeError) {
            throw severeError;
        }
        throw new RuntimeException(error);
    }

    public <R> Try<R> map(Function<? super T, ? extends R> fn) {
        if (isFailure()) {
            return new Try<>(null, error);
        }
        try {
            return new Try<>(fn.apply(value), null);
        } catch (Throwable t) {
            return new Try<>(null, t);
        }
    }

    public <R> Try<R> flatMap(Function<? super T, Try<R>> fn) {
        if (isFailure()) {
            return new Try<>(null, error);
        }
        try {
            Try<R> next = fn.apply(value);
            return next == null ? new Try<>(null, new NullPointerException("flatMap returned null")) : next;
        } catch (Throwable t) {
            return new Try<>(null, t);
        }
    }

    public Try<T> recover(Function<? super Throwable, ? extends T> fn) {
        if (isSuccess()) {
            return this;
        }
        try {
            return new Try<>(fn.apply(error), null);
        } catch (Throwable t) {
            return new Try<>(null, t);
        }
    }

    public Try<T> onSuccess(Consumer<? super T> action) {
        if (isSuccess()) {
            action.accept(value);
        }
        return this;
    }

    public Try<T> onFailure(Consumer<? super Throwable> action) {
        if (isFailure()) {
            action.accept(error);
        }
        return this;
    }

    public Throwable getError() {
        return error;
    }
}
