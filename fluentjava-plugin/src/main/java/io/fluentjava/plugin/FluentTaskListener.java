package io.fluentjava.plugin;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

/**
 * Task listener that intercepts the PARSE phase to transform each
 * compilation unit's AST.
 *
 * <p>Uses the {@link FluentTargetRegistry} to dynamically discover
 * which methods to rewrite and which imports to inject. Adding a new
 * static method to any FluentXxx class automatically makes it
 * available — <b>zero changes needed here</b>.</p>
 *
 * @since 1.0.0
 * @see FluentTargetRegistry
 */
public class FluentTaskListener implements TaskListener {

    private final FluentImportInjector importInjector;
    private final FluentMethodRewriter methodRewriter;

    /**
     * Creates a new task listener.
     *
     * @param task the javac task (used to obtain the compilation context)
     */
    public FluentTaskListener(JavacTask task) {
        FluentTargetRegistry registry = new FluentTargetRegistry();
        this.importInjector = new FluentImportInjector(task, registry);
        this.methodRewriter = new FluentMethodRewriter(task, registry);
    }

    @Override
    public void started(TaskEvent event) {
        // Nothing to do before parsing
    }

    /**
     * Called when a compilation phase finishes.
     * We intercept {@code PARSE} to transform the AST before resolution.
     *
     * @param event the task event
     */
    @Override
    public void finished(TaskEvent event) {
        if (event.getKind() != TaskEvent.Kind.PARSE) {
            return;
        }

        var compilationUnit = event.getCompilationUnit();
        if (compilationUnit == null) {
            return;
        }

        // Step 1: Inject static imports into the AST
        importInjector.inject(compilationUnit);

        // Step 2: Rewrite fluent method calls → static method calls
        methodRewriter.rewrite(compilationUnit);
    }
}
