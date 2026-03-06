package io.fluentjava.plugin;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.Set;

/**
 * Rewrites fluent method calls in the AST from receiver-style to static-style.
 *
 * <h3>Transformation:</h3>
 * <pre>
 *   BEFORE (developer writes):   "test".isBlankSafe()
 *   AFTER  (rewritten AST):      isBlankSafe("test")
 *   Bytecode produced:            invokestatic FluentString.isBlankSafe(String)
 * </pre>
 *
 * <p>Method names are discovered <b>dynamically</b> from the {@link FluentTargetRegistry},
 * which scans all FluentXxx classes via reflection. Adding a new method to
 * any FluentXxx class automatically makes it available for rewriting —
 * <b>zero configuration needed here</b>.</p>
 *
 * @since 1.0.0
 * @see FluentTargetRegistry
 */
public class FluentMethodRewriter {

    private final Set<String> fluentMethodNames;
    private final Set<String> fluentClassNames;
    private final TreeMaker treeMaker;
    private final Names names;

    /**
     * Creates a new method rewriter.
     *
     * @param task     the javac task (used to obtain TreeMaker and Names for AST construction)
     * @param registry the dynamic target registry (auto-discovers method names)
     */
    public FluentMethodRewriter(JavacTask task, FluentTargetRegistry registry) {
        Context context = ((BasicJavacTask) task).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.fluentMethodNames = registry.getMethodNames();
        this.fluentClassNames = registry.getFluentClassSimpleNames();
    }

    /**
     * Rewrites all fluent method calls in the given compilation unit.
     *
     * @param compilationUnit the parsed source file's AST
     */
    public void rewrite(CompilationUnitTree compilationUnit) {
        if (!(compilationUnit instanceof JCCompilationUnit jcUnit)) {
            return;
        }
        jcUnit.accept(new RewriteScanner());
    }

    private class RewriteScanner extends TreeScanner {

        @Override
        public void visitApply(JCMethodInvocation invocation) {
            // First, recurse into children (handles nested calls like a.b().c())
            super.visitApply(invocation);

            // Only process receiver.method(args) patterns
            if (!(invocation.meth instanceof JCFieldAccess fieldAccess)) {
                return;
            }

            String methodName = fieldAccess.name.toString();

            // Only rewrite if it's a registered fluent method (auto-discovered)
            if (!fluentMethodNames.contains(methodName)) {
                return;
            }

            // Skip already-static calls like FluentString.isBlankSafe(x)
            JCExpression receiver = fieldAccess.selected;
            if (receiver instanceof JCIdent ident
                    && fluentClassNames.contains(ident.name.toString())) {
                return;
            }

            // ── Perform the rewrite ──────────────────────────────────────
            //
            //   BEFORE:  receiver.methodName(arg1, arg2)
            //            meth = JCFieldAccess { selected=receiver, name=methodName }
            //            args = [arg1, arg2]
            //
            //   AFTER:   methodName(receiver, arg1, arg2)
            //            meth = JCIdent { name=methodName }
            //            args = [receiver, arg1, arg2]
            //

            // 1) Replace meth: JCFieldAccess → JCIdent(methodName)
            //    Use TreeMaker to construct a proper JCIdent node
            invocation.meth = treeMaker.at(fieldAccess.pos)
                    .Ident(names.fromString(methodName));

            // 2) Prepend receiver to argument list
            invocation.args = invocation.args.prepend(receiver);
        }
    }
}
