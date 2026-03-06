package io.fluentjava.plugin;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Dynamic registry that discovers FluentJava target classes and their methods
 * automatically at compile-time via reflection.
 *
 * <p>Reads {@code META-INF/fluentjava/targets} from the classpath to discover
 * FluentXxx utility classes (e.g., {@code FluentString}, {@code FluentList}),
 * then uses reflection to extract all {@code public static} method names.</p>
 *
 * <h3>Adding a new fluent method:</h3>
 * <p>Just add a {@code public static} method to the appropriate FluentXxx class.
 * The registry discovers it automatically — <b>zero configuration needed
 * in the javac plugin</b>.</p>
 *
 * <h3>Adding a new target type (e.g., FluentMap):</h3>
 * <ol>
 *   <li>Create the FluentXxx class in {@code fluentjava-runtime}</li>
 *   <li>Add its FQN to {@code META-INF/fluentjava/targets}</li>
 *   <li>That's it — the javac plugin auto-imports and auto-rewrites</li>
 * </ol>
 *
 * @since 1.1.0
 */
public final class FluentTargetRegistry {

    private static final String TARGETS_RESOURCE = "META-INF/fluentjava/targets";

    /** All discovered fluent method names (union across all target classes). */
    private final Set<String> methodNames;

    /** Fully qualified class names to import (e.g., "io.fluentjava.string.FluentString"). */
    private final List<String> importTargets;

    /** Simple class names of FluentXxx classes (for skipping already-static calls). */
    private final Set<String> fluentClassSimpleNames;

    /**
     * Creates a new registry by discovering target classes from the classpath.
     * Falls back to hardcoded defaults if discovery fails.
     */
    public FluentTargetRegistry() {
        Set<String> methods = new LinkedHashSet<>();
        List<String> imports = new ArrayList<>();
        Set<String> classNames = new LinkedHashSet<>();

        List<String> targetClassNames = loadTargetClassNames();

        for (String className : targetClassNames) {
            try {
                Class<?> clazz = Class.forName(className, false,
                        FluentTargetRegistry.class.getClassLoader());

                imports.add(className);
                classNames.add(clazz.getSimpleName());

                for (Method m : clazz.getDeclaredMethods()) {
                    if (Modifier.isPublic(m.getModifiers())
                            && Modifier.isStatic(m.getModifiers())) {
                        methods.add(m.getName());
                    }
                }
            } catch (ClassNotFoundException e) {
                // Class not on classpath — skip silently
            }
        }

        // Fallback: if nothing was discovered, use hardcoded defaults
        if (methods.isEmpty()) {
            methods.addAll(Set.of(
                    "isBlankSafe", "trimToNull", "toIntOrNull", "mask",
                    "firstOrNull", "lastOrNull", "filterBy", "mapTo"
            ));
            imports.addAll(List.of(
                    "io.fluentjava.string.FluentString",
                    "io.fluentjava.list.FluentList"
            ));
            classNames.addAll(Set.of("FluentString", "FluentList"));
        }

        this.methodNames = Collections.unmodifiableSet(methods);
        this.importTargets = Collections.unmodifiableList(imports);
        this.fluentClassSimpleNames = Collections.unmodifiableSet(classNames);
    }

    /**
     * Returns all discovered fluent method names.
     */
    public Set<String> getMethodNames() {
        return methodNames;
    }

    /**
     * Returns FQNs of all FluentXxx classes (for import injection).
     */
    public List<String> getImportTargets() {
        return importTargets;
    }

    /**
     * Returns simple class names of FluentXxx classes (for skip detection).
     */
    public Set<String> getFluentClassSimpleNames() {
        return fluentClassSimpleNames;
    }

    /**
     * Reads target class names from {@code META-INF/fluentjava/targets}.
     */
    private static List<String> loadTargetClassNames() {
        List<String> names = new ArrayList<>();
        ClassLoader cl = FluentTargetRegistry.class.getClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader();

        try (InputStream is = cl.getResourceAsStream(TARGETS_RESOURCE)) {
            if (is == null) {
                return names;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        names.add(line);
                    }
                }
            }
        } catch (IOException e) {
            // Silently fall through — the fallback will kick in
        }
        return names;
    }
}
