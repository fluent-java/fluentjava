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

    /**
     * Internal mutable collections — updated during classpath discovery,
     * source scanning, AND AST-based discovery (two-phase compile).
     *
     * <p>{@link FluentMethodRewriter} and {@link FluentImportInjector} store
     * unmodifiable <em>views</em> (via {@link Collections#unmodifiableSet} etc.)
     * that read through to these backing collections. Any additions made by
     * {@link #addMethodName}, {@link #addImportTarget}, or
     * {@link #addFluentClassName} are therefore immediately visible to the
     * rewriter/injector without re-creating them.</p>
     */
    private final Set<String> methodNames = new LinkedHashSet<>();
    private final List<String> importTargets = new ArrayList<>();
    private final Set<String> fluentClassSimpleNames = new LinkedHashSet<>();

    /**
     * Creates a new registry by discovering target classes from the classpath.
     * Falls back to hardcoded defaults if discovery fails.
     */
    public FluentTargetRegistry() {
        List<String> targetClassNames = loadTargetClassNames();

        for (String className : targetClassNames) {
            try {
                Class<?> clazz = Class.forName(className, false,
                        FluentTargetRegistry.class.getClassLoader());

                importTargets.add(className);
                fluentClassSimpleNames.add(clazz.getSimpleName());

                for (Method m : clazz.getDeclaredMethods()) {
                    if (Modifier.isPublic(m.getModifiers())
                            && Modifier.isStatic(m.getModifiers())) {
                        methodNames.add(m.getName());
                    }
                }
            } catch (ClassNotFoundException e) {
                // Class not on classpath — skip silently
            }
        }

        // Also discover extension methods from current module sources so
        // newly added project extensions are usable in the same compile.
        discoverFromProjectSources(methodNames, importTargets, fluentClassSimpleNames);

        // Fallback: if nothing was discovered, use hardcoded defaults
        if (methodNames.isEmpty()) {
            methodNames.addAll(Set.of(
                    "isBlankSafe", "trimToNull", "toIntOrNull", "mask",
                    "firstOrNull", "lastOrNull", "filterBy", "mapTo"
            ));
            importTargets.addAll(List.of(
                    "io.fluentjava.string.FluentString",
                    "io.fluentjava.list.FluentList"
            ));
            fluentClassSimpleNames.addAll(Set.of("FluentString", "FluentList"));
        }
    }

    // ── Dynamic registration (called from FluentTaskListener AST scan) ──

    /** Registers a method name discovered from a parsed @FluentExtension AST. */
    public void addMethodName(String name) {
        methodNames.add(name);
    }

    /** Registers an import target (extension class FQN) for static import injection. */
    public void addImportTarget(String fqn) {
        if (!importTargets.contains(fqn)) {
            importTargets.add(fqn);
        }
    }

    /** Registers a simple class name for already-static-call detection. */
    public void addFluentClassName(String simpleName) {
        fluentClassSimpleNames.add(simpleName);
    }

    // ── Getters (return live unmodifiable views) ──────────────────────

    /**
     * Returns all discovered fluent method names.
     * The returned view reflects additions made after construction.
     */
    public Set<String> getMethodNames() {
        return Collections.unmodifiableSet(methodNames);
    }

    /**
     * Returns FQNs of all FluentXxx/extension classes (for import injection).
     * The returned view reflects additions made after construction.
     */
    public List<String> getImportTargets() {
        return Collections.unmodifiableList(importTargets);
    }

    /**
     * Returns simple class names (for skip detection).
     * The returned view reflects additions made after construction.
     */
    public Set<String> getFluentClassSimpleNames() {
        return Collections.unmodifiableSet(fluentClassSimpleNames);
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
        Path root = Paths.get(System.getProperty("user.dir", "."));

        // Collect all candidate source roots (handles multi-module layouts)
        List<Path> candidateRoots = new ArrayList<>();

        // 1. Standard Maven/Gradle: user.dir/src/main/java
        Path directSrc = root.resolve("src").resolve("main").resolve("java");
        if (Files.isDirectory(directSrc)) {
            candidateRoots.add(directSrc);
        }

        // 2. Multi-module children: user.dir/*/src/main/java
        try (var children = Files.list(root)) {
            children.filter(Files::isDirectory)
                    .map(dir -> dir.resolve("src").resolve("main").resolve("java"))
                    .filter(Files::isDirectory)
                    .forEach(candidateRoots::add);
        } catch (IOException ignored) {}

        // 3. Parent module (if CWD is a sub-module): ../src/main/java, ../*/src/main/java
        Path parent = root.getParent();
        if (parent != null) {
            Path parentSrc = parent.resolve("src").resolve("main").resolve("java");
            if (Files.isDirectory(parentSrc)) {
                candidateRoots.add(parentSrc);
            }
            try (var siblings = Files.list(parent)) {
                siblings.filter(Files::isDirectory)
                        .filter(dir -> !dir.equals(root))
                        .map(dir -> dir.resolve("src").resolve("main").resolve("java"))
                        .filter(Files::isDirectory)
                        .forEach(candidateRoots::add);
            } catch (IOException ignored) {}
        }

        for (Path sourceRoot : candidateRoots) {
            try (var stream = Files.walk(sourceRoot)) {
                stream.filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> scanExtensionSource(path, methods, imports, classNames));
            } catch (IOException ignored) {}
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
