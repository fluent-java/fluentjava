package io.fluentjava.plugin;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.List;

/**
 * Injects static import declarations into the AST of each compilation unit.
 *
 * <p>Import targets are discovered <b>dynamically</b> from the
 * {@link FluentTargetRegistry}. Adding a new FluentXxx class to
 * {@code META-INF/fluentjava/targets} automatically includes its
 * import — <b>zero configuration needed here</b>.</p>
 *
 * @since 1.0.0
 * @see FluentTargetRegistry
 */
public class FluentImportInjector {

    private final List<String> importTargets;
    private final TreeMaker treeMaker;
    private final Names names;

    /**
     * Creates a new import injector.
     *
     * @param task     the javac task (used to obtain TreeMaker and Names)
     * @param registry the dynamic target registry (auto-discovers import targets)
     */
    public FluentImportInjector(JavacTask task, FluentTargetRegistry registry) {
        Context context = ((BasicJavacTask) task).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.importTargets = registry.getImportTargets();
    }

    /**
     * Injects static wildcard imports into the given compilation unit.
     *
     * <p>After this method, the AST will contain:
     * {@code import static io.fluentjava.string.FluentString.*;} and
     * {@code import static io.fluentjava.list.FluentList.*;}.</p>
     *
     * @param compilationUnit the parsed source file's AST
     */
    public void inject(CompilationUnitTree compilationUnit) {
        if (!(compilationUnit instanceof JCCompilationUnit jcUnit)) {
            return;
        }

        // Collect import nodes to add
        com.sun.tools.javac.util.List<JCTree> newImports = com.sun.tools.javac.util.List.nil();
        for (String target : importTargets) {
            if (alreadyImported(jcUnit, target)) {
                continue;
            }
            newImports = newImports.prepend(buildStaticWildcardImport(target));
        }

        if (newImports.isEmpty()) {
            return;
        }

        // Insert new imports AFTER the package declaration.
        // jcUnit.defs contains: [package-decl?, import*, class-decl*]
        // We split at the package declaration to avoid displacing it.
        var defs = jcUnit.defs;
        com.sun.tools.javac.util.List<JCTree> before = com.sun.tools.javac.util.List.nil();

        // Consume the package declaration if present
        if (!defs.isEmpty() && defs.head.hasTag(com.sun.tools.javac.tree.JCTree.Tag.PACKAGEDEF)) {
            before = before.prepend(defs.head);
            defs = defs.tail;
        }

        // Append: before (package) + newImports + rest (existing imports + classes)
        var result = defs;
        for (JCTree imp : newImports) {
            result = result.prepend(imp);
        }
        for (JCTree b : before) {
            result = result.prepend(b);
        }

        jcUnit.defs = result;
    }

    /**
     * Builds an AST node for: {@code import static <fqcn>.*;}
     *
     * @param fullyQualifiedClassName e.g. "io.fluentjava.string.FluentString"
     * @return the JCImport node
     */
    private JCImport buildStaticWildcardImport(String fullyQualifiedClassName) {
        // Build the qualified name: io.fluentjava.string.FluentString
        String[] parts = fullyQualifiedClassName.split("\\.");
        JCExpression qualifier = treeMaker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++) {
            qualifier = treeMaker.Select(qualifier, names.fromString(parts[i]));
        }

        // Append .* for wildcard import
        JCExpression wildcardImport = treeMaker.Select(qualifier, names.asterisk);

        // Create: import static <fqcn>.*;
        return treeMaker.Import(wildcardImport, /* static = */ true);
    }

    /**
     * Checks if the compilation unit already contains a static import for
     * the given class (to avoid duplicates on incremental compilation).
     */
    private boolean alreadyImported(JCCompilationUnit unit, String targetClass) {
        for (JCTree def : unit.defs) {
            if (def instanceof JCImport imp && imp.isStatic()) {
                String importText = imp.qualid.toString();
                if (importText.startsWith(targetClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
