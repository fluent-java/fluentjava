package io.fluentjava.plugin;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String EXTENSIONS_RESOURCE = "META-INF/fluentjava/extensions";
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern TYPE_PATTERN = Pattern.compile("(?m)^\\s*(?:public\\s+)?(?:final\\s+|abstract\\s+)?(?:class|record|interface|enum)\\s+([A-Za-z_][A-Za-z0-9_]*)\\b");
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("@(?:[\\w.]+\\.)?FluentExtension\\s*\\(\\s*target\\s*=\\s*([\\w.$]+)\\s*\\.class\\s*\\)");
    private static final Pattern PUBLIC_STATIC_METHOD_PATTERN = Pattern.compile("(?m)^\\s*public\\s+static\\s+[\\w$<>\\[\\], ?]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");

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

        // Also discover extension methods from current module sources so
        // newly added project extensions are usable in the same compile.
        discoverFromProjectSources(methods, imports, classNames);

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
     * Reads target class names from both built-in and extension resources.
     */
    private static List<String> loadTargetClassNames() {
        Set<String> names = new LinkedHashSet<>();
        ClassLoader cl = FluentTargetRegistry.class.getClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader();

        loadFromAllResources(cl, TARGETS_RESOURCE, names);
        loadFromAllResources(cl, EXTENSIONS_RESOURCE, names);

        return new ArrayList<>(names);
    }

    private static void loadFromAllResources(ClassLoader cl, String resourceName, Set<String> names) {
        try {
            Enumeration<URL> urls = cl.getResources(resourceName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream is = url.openStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            names.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Silently fall through — the fallback will kick in
        }
    }

    private static void discoverFromProjectSources(Set<String> methods,
                                                   List<String> imports,
                                                   Set<String> classNames) {
        Path sourceRoot = Paths.get(System.getProperty("user.dir", "."), "src", "main", "java");
        if (!Files.isDirectory(sourceRoot)) {
            return;
        }

        try (var stream = Files.walk(sourceRoot)) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> scanExtensionSource(path, methods, imports, classNames));
        } catch (IOException ignored) {
            // Keep best-effort behavior; runtime discovery and fallback still apply.
        }
    }

    private static void scanExtensionSource(Path javaFile,
                                            Set<String> methods,
                                            List<String> imports,
                                            Set<String> classNames) {
        String source;
        try {
            source = Files.readString(javaFile, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return;
        }

        Matcher extensionMatcher = EXTENSION_PATTERN.matcher(source);
        if (!extensionMatcher.find()) {
            return;
        }

        String pkg = "";
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(source);
        if (packageMatcher.find()) {
            pkg = packageMatcher.group(1);
        }

        Matcher typeMatcher = TYPE_PATTERN.matcher(source);
        if (!typeMatcher.find()) {
            return;
        }

        String simpleName = typeMatcher.group(1);
        String fqn = pkg.isEmpty() ? simpleName : pkg + "." + simpleName;
        imports.add(fqn);
        classNames.add(simpleName);

        Matcher methodMatcher = PUBLIC_STATIC_METHOD_PATTERN.matcher(source);
        while (methodMatcher.find()) {
            methods.add(methodMatcher.group(1));
        }
    }
}
