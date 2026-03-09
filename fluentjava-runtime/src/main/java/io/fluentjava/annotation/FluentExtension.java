package io.fluentjava.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a FluentJava external extension provider.
 *
 * <p>The annotated class should expose {@code public static} methods where the first
 * parameter is the receiver type declared by {@link #target()}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FluentExtension {

    /**
     * Receiver type for extension methods declared in the annotated class.
     */
    Class<?> target();
}
