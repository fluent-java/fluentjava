package io.fluentjava.plugin;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

/**
 * Javac plugin entry point for FluentJava.
 *
 * <p>This class implements {@link com.sun.source.util.Plugin} — the <strong>official
 * public API</strong> for javac plugins, available since JDK 8. It is activated via:
 * </p>
 * <pre>
 *   javac -Xplugin:FluentJava MyClass.java
 * </pre>
 *
 * <h3>What happens at compile-time:</h3>
 * <ol>
 *   <li>{@code javac} discovers this plugin via
 *       {@code META-INF/services/com.sun.source.util.Plugin}</li>
 *   <li>{@link #init(JavacTask, String...)} registers a {@link FluentTaskListener}
 *       on the compilation task</li>
 *   <li>After each source file is parsed (PARSE phase), the listener:
 *       <ul>
 *         <li>Injects static imports for {@code FluentString.*} and
 *             {@code FluentList.*} into the AST</li>
 *         <li>Rewrites fluent calls ({@code obj.method(args)}) into
 *             static calls ({@code method(obj, args)})</li>
 *       </ul>
 *   </li>
 *   <li>{@code javac} continues to RESOLVE phase — it resolves the rewritten
 *       method calls via the injected static imports</li>
 *   <li>Final bytecode contains only {@code invokestatic} calls</li>
 * </ol>
 *
 * <h3>Security guarantees:</h3>
 * <ul>
 *   <li>This plugin runs <strong>only at compile-time</strong> (dev machine / CI)</li>
 *   <li>The plugin JAR is {@code scope:provided} — <strong>not shipped</strong> to production</li>
 *   <li>Production JVM starts with: {@code java -jar app.jar} — zero agents, zero add-opens</li>
 *   <li>Uses {@code com.sun.source.util.Plugin} — official public API</li>
 *   <li>Uses {@code com.sun.source.tree.*} — official public API</li>
 *   <li>Uses {@code com.sun.tools.javac.tree.*} — compile-time only, same as every
 *       major Java tool (Lombok, Error Prone, Checker Framework)</li>
 * </ul>
 *
 * @since 1.0.0
 * @see FluentTaskListener
 * @see FluentImportInjector
 * @see FluentMethodRewriter
 */
public class FluentJavaPlugin implements Plugin {

    /**
     * The plugin name, used with {@code -Xplugin:FluentJava}.
     */
    private static final String PLUGIN_NAME = "FluentJava";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    /**
     * Called by javac when the plugin is loaded.
     * Registers the {@link FluentTaskListener} for AST transformation.
     *
     * @param task the javac compilation task
     * @param args optional plugin arguments (not used)
     */
    @Override
    public void init(JavacTask task, String... args) {
        task.addTaskListener(new FluentTaskListener(task));
    }
}
