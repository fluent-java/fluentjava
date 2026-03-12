package io.fluentjava.plugin;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;

/**
 * Task listener that uses a <b>two-phase</b> approach to transform each
 * compilation unit's AST — guaranteeing that {@code @FluentExtension} methods
 * defined in the same module are discovered before any rewriting happens.
 *
 * <h3>Phase 1 — PARSE (collect &amp; discover)</h3>
 * <p>As each source file is parsed, we:</p>
 * <ol>
 *   <li>Store the compilation unit for later processing</li>
 *   <li>Scan the AST for {@code @FluentExtension} classes and register their
 *       public static methods in the {@link FluentTargetRegistry} (which
 *       in turn makes them visible to the rewriter and import injector)</li>
 * </ol>
 *
 * <h3>Phase 2 — ENTER (rewrite)</h3>
 * <p>When the first {@code ENTER} event starts, all PARSE events have
 * completed. We process <b>every</b> collected compilation unit:</p>
 * <ol>
 *   <li>Inject static imports ({@link FluentImportInjector})</li>
 *   <li>Rewrite fluent calls ({@link FluentMethodRewriter})</li>
 * </ol>
 * <p>Because we modify the AST in {@code started(ENTER)} — before javac
 * enters the first CU — the changes are visible to the compiler's
 * resolution phase.</p>
 *
 * @since 1.0.0
 * @see FluentTargetRegistry
 */
public class FluentTaskListener implements TaskListener {

    private final FluentTargetRegistry registry;
    private final FluentImportInjector importInjector;
    private final FluentMethodRewriter methodRewriter;

    /** CUs collected during PARSE, to be processed at first ENTER. */
    private final List<CompilationUnitTree> pendingUnits = new ArrayList<>();
    private boolean processed = false;

    public FluentTaskListener(JavacTask task) {
        this.registry = new FluentTargetRegistry();
        this.importInjector = new FluentImportInjector(task, registry);
        this.methodRewriter = new FluentMethodRewriter(task, registry);
    }

    @Override
    public void started(TaskEvent event) {
        if (event.getKind() == TaskEvent.Kind.ENTER && !processed) {
            processed = true;
            // All PARSE events have fired — registry now contains both built-in
            // and AST-discovered extension methods.
            for (CompilationUnitTree unit : pendingUnits) {
                importInjector.inject(unit);
                methodRewriter.rewrite(unit);
            }
            pendingUnits.clear();
        }
    }

    @Override
    public void finished(TaskEvent event) {
        if (event.getKind() != TaskEvent.Kind.PARSE) {
            return;
        }

        var compilationUnit = event.getCompilationUnit();
        if (compilationUnit == null) {
            return;
        }

        // Scan AST for @FluentExtension classes → register in the shared registry
        scanForExtensions(compilationUnit);

        // Defer import injection + rewriting to the ENTER phase
        pendingUnits.add(compilationUnit);
    }

    /**
     * Scans a parsed compilation unit for {@code @FluentExtension} classes
     * and registers their public static methods in the registry.
     */
    private void scanForExtensions(CompilationUnitTree cu) {
        if (!(cu instanceof JCCompilationUnit jcUnit)) return;

        // Use the public API to get the package name
        String pkg = cu.getPackageName() != null ? cu.getPackageName().toString() : "";

        for (JCTree def : jcUnit.defs) {
            if (!(def instanceof JCClassDecl classDecl)) continue;

            boolean isExtension = false;
            for (JCAnnotation ann : classDecl.mods.annotations) {
                String annName = ann.annotationType.toString();
                if (annName.equals("FluentExtension")
                        || annName.endsWith(".FluentExtension")) {
                    isExtension = true;
                    break;
                }
            }
            if (!isExtension) continue;

            String simpleName = classDecl.name.toString();
            String fqn = pkg.isEmpty() ? simpleName : pkg + "." + simpleName;

            registry.addImportTarget(fqn);
            registry.addFluentClassName(simpleName);

            for (JCTree member : classDecl.defs) {
                if (!(member instanceof JCMethodDecl methodDecl)) continue;
                if (methodDecl.name.toString().equals("<init>")) continue;

                var flags = methodDecl.mods.getFlags();
                if (flags.contains(Modifier.PUBLIC) && flags.contains(Modifier.STATIC)) {
                    registry.addMethodName(methodDecl.name.toString());
                }
            }
        }
    }
}
