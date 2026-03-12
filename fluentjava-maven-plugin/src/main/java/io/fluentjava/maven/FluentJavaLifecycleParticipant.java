package io.fluentjava.maven;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lifecycle participant that configures the Maven build for FluentJava
 * <b>before</b> lifecycle computation. This ensures the compiler plugin
 * picks up the injected configuration (fork, compilerArgs, dependency).
 *
 * <p>Activated when the user declares {@code <extensions>true</extensions>}
 * on the {@code fluentjava-maven-plugin}.</p>
 */
public class FluentJavaLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    private static final Logger LOG = LoggerFactory.getLogger(FluentJavaLifecycleParticipant.class);

    private static final String FLUENTJAVA_GROUP = "io.fluentjava";
    private static final String FLUENTJAVA_MAVEN_PLUGIN = "fluentjava-maven-plugin";
    private static final String FLUENTJAVA_PLUGIN_ARTIFACT = "fluentjava-plugin";

    private static final String COMPILER_GROUP = "org.apache.maven.plugins";
    private static final String COMPILER_ARTIFACT = "maven-compiler-plugin";
    private static final String COMPILER_VERSION = "3.12.1";

    private static final String XPLUGIN_ARG = "-Xplugin:FluentJava";
    private static final List<String> ADD_EXPORTS = List.of(
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
    );

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        for (MavenProject project : session.getProjects()) {
            Plugin ourPlugin = findOurPlugin(project);
            if (ourPlugin != null) {
                String version = ourPlugin.getVersion() != null
                        ? ourPlugin.getVersion() : "1.0.0";
                LOG.info("[FluentJava] Injecting compiler args...");
                addPluginDependency(project, version);
                configureCompilerPlugin(project);
                ensureConfigureExecution(ourPlugin);
                LOG.info("[FluentJava] Configured: fork=true");
                LOG.info("[FluentJava] Configured: -Xplugin:FluentJava");
                LOG.info("[FluentJava] Configured: --add-exports ({} exports)", ADD_EXPORTS.size());
            }
        }
    }

    private Plugin findOurPlugin(MavenProject project) {
        if (project.getBuild() == null) {
            return null;
        }
        for (Plugin p : project.getBuild().getPlugins()) {
            if (FLUENTJAVA_GROUP.equals(p.getGroupId())
                    && FLUENTJAVA_MAVEN_PLUGIN.equals(p.getArtifactId())) {
                return p;
            }
        }
        return null;
    }

    private void addPluginDependency(MavenProject project, String version) {
        for (Dependency dep : project.getDependencies()) {
            if (FLUENTJAVA_PLUGIN_ARTIFACT.equals(dep.getArtifactId())
                    && FLUENTJAVA_GROUP.equals(dep.getGroupId())) {
                return;
            }
        }

        Dependency dep = new Dependency();
        dep.setGroupId(FLUENTJAVA_GROUP);
        dep.setArtifactId(FLUENTJAVA_PLUGIN_ARTIFACT);
        dep.setVersion(version);
        dep.setScope("provided");
        project.getDependencies().add(dep);
        LOG.info("[FluentJava] Adding dependency: fluentjava-plugin (provided)");
    }

    private void configureCompilerPlugin(MavenProject project) {
        // Find or create the compiler plugin in build/plugins
        Plugin compiler = null;
        for (Plugin p : project.getBuild().getPlugins()) {
            if (COMPILER_GROUP.equals(p.getGroupId())
                    && COMPILER_ARTIFACT.equals(p.getArtifactId())) {
                compiler = p;
                break;
            }
        }

        if (compiler == null) {
            compiler = new Plugin();
            compiler.setGroupId(COMPILER_GROUP);
            compiler.setArtifactId(COMPILER_ARTIFACT);
            project.getBuild().addPlugin(compiler);
        }
        compiler.setVersion(COMPILER_VERSION);

        // Maven 3.x only merges EXECUTION-level config for default lifecycle
        // bindings, not plugin-level config. So we add our configuration to a
        // PluginExecution with id "default-compile" (the standard lifecycle id).
        PluginExecution compileExec = null;
        for (PluginExecution exec : compiler.getExecutions()) {
            if ("default-compile".equals(exec.getId())) {
                compileExec = exec;
                break;
            }
        }
        if (compileExec == null) {
            compileExec = new PluginExecution();
            compileExec.setId("default-compile");
            compileExec.setPhase("compile");
            compileExec.addGoal("compile");
            compiler.addExecution(compileExec);
        }

        applyCompilerConfig(compileExec);

        // Also set plugin-level config and pluginManagement for broader compat
        applyCompilerConfig(compiler);

        PluginManagement pm = project.getBuild().getPluginManagement();
        if (pm == null) {
            pm = new PluginManagement();
            project.getBuild().setPluginManagement(pm);
        }
        Plugin pmCompiler = null;
        for (Plugin p : pm.getPlugins()) {
            if (COMPILER_GROUP.equals(p.getGroupId())
                    && COMPILER_ARTIFACT.equals(p.getArtifactId())) {
                pmCompiler = p;
                break;
            }
        }
        if (pmCompiler == null) {
            pmCompiler = new Plugin();
            pmCompiler.setGroupId(COMPILER_GROUP);
            pmCompiler.setArtifactId(COMPILER_ARTIFACT);
            pm.addPlugin(pmCompiler);
        }
        pmCompiler.setVersion(COMPILER_VERSION);
        applyCompilerConfig(pmCompiler);
    }

    private void applyCompilerConfig(Object target) {
        Xpp3Dom config;
        if (target instanceof PluginExecution exec) {
            config = (Xpp3Dom) exec.getConfiguration();
            if (config == null) {
                config = new Xpp3Dom("configuration");
                exec.setConfiguration(config);
            }
        } else if (target instanceof Plugin plugin) {
            config = (Xpp3Dom) plugin.getConfiguration();
            if (config == null) {
                config = new Xpp3Dom("configuration");
                plugin.setConfiguration(config);
            }
        } else {
            return;
        }

        // fork = true
        Xpp3Dom fork = config.getChild("fork");
        if (fork == null) {
            fork = new Xpp3Dom("fork");
            fork.setValue("true");
            config.addChild(fork);
        } else if (!"true".equals(fork.getValue())) {
            fork.setValue("true");
        }

        // compilerArgs
        Xpp3Dom compilerArgs = config.getChild("compilerArgs");
        if (compilerArgs == null) {
            compilerArgs = new Xpp3Dom("compilerArgs");
            config.addChild(compilerArgs);
        }

        addArgIfMissing(compilerArgs, XPLUGIN_ARG);
        for (String exp : ADD_EXPORTS) {
            addArgIfMissing(compilerArgs, exp);
        }
    }

    private void addArgIfMissing(Xpp3Dom compilerArgs, String value) {
        for (Xpp3Dom child : compilerArgs.getChildren()) {
            if (value.equals(child.getValue())) {
                return;
            }
        }
        Xpp3Dom arg = new Xpp3Dom("arg");
        arg.setValue(value);
        compilerArgs.addChild(arg);
    }

    private void ensureConfigureExecution(Plugin plugin) {
        for (PluginExecution exec : plugin.getExecutions()) {
            if (exec.getGoals().contains("configure")) {
                return;
            }
        }
        PluginExecution exec = new PluginExecution();
        exec.setId("fluentjava-auto-configure");
        exec.setPhase("initialize");
        exec.addGoal("configure");
        plugin.addExecution(exec);
    }
}
