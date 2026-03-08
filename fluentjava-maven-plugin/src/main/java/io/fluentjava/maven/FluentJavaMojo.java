package io.fluentjava.maven;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;

/**
 * Maven Mojo that automatically configures the {@code maven-compiler-plugin}
 * for FluentJava. Injects the required compiler arguments, fork mode, and
 * annotation processor path so that users don't need to write boilerplate
 * configuration.
 *
 * <p>Usage in {@code pom.xml}:</p>
 * <pre>{@code
 * <plugin>
 *     <groupId>io.fluentjava</groupId>
 *     <artifactId>fluentjava-maven-plugin</artifactId>
 *     <version>1.0.0</version>
 *     <executions>
 *         <execution>
 *             <goals><goal>configure</goal></goals>
 *         </execution>
 *     </executions>
 * </plugin>
 * }</pre>
 *
 * @since 1.0.0
 */
@Mojo(name = "configure", defaultPhase = LifecyclePhase.INITIALIZE)
public class FluentJavaMojo extends AbstractMojo {

    private static final String COMPILER_PLUGIN_GROUP = "org.apache.maven.plugins";
    private static final String COMPILER_PLUGIN_ARTIFACT = "maven-compiler-plugin";
    private static final String COMPILER_PLUGIN_VERSION = "3.12.1";

    private static final List<String> REQUIRED_COMPILER_ARGS = List.of(
            "-Xplugin:FluentJava",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"
    );

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("FluentJava: configuring maven-compiler-plugin...");

        Plugin compiler = findOrCreateCompilerPlugin();
        Xpp3Dom config = getOrCreateConfiguration(compiler);

        configureFork(config);
        configureCompilerArgs(config);
        configureAnnotationProcessorPaths(config);

        getLog().info("FluentJava configured successfully.");
    }

    private Plugin findOrCreateCompilerPlugin() {
        for (Plugin plugin : project.getBuild().getPlugins()) {
            if (COMPILER_PLUGIN_GROUP.equals(plugin.getGroupId())
                    && COMPILER_PLUGIN_ARTIFACT.equals(plugin.getArtifactId())) {
                getLog().debug("FluentJava: found existing maven-compiler-plugin");
                return plugin;
            }
        }

        getLog().debug("FluentJava: creating maven-compiler-plugin declaration");
        Plugin compiler = new Plugin();
        compiler.setGroupId(COMPILER_PLUGIN_GROUP);
        compiler.setArtifactId(COMPILER_PLUGIN_ARTIFACT);
        compiler.setVersion(COMPILER_PLUGIN_VERSION);
        project.getBuild().addPlugin(compiler);
        return compiler;
    }

    private Xpp3Dom getOrCreateConfiguration(Plugin plugin) {
        Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
        if (config == null) {
            config = new Xpp3Dom("configuration");
            plugin.setConfiguration(config);
        }
        return config;
    }

    private void configureFork(Xpp3Dom config) {
        Xpp3Dom fork = config.getChild("fork");
        if (fork == null) {
            fork = new Xpp3Dom("fork");
            fork.setValue("true");
            config.addChild(fork);
            getLog().debug("FluentJava: set fork=true");
        } else if (!"true".equals(fork.getValue())) {
            fork.setValue("true");
            getLog().debug("FluentJava: overrode fork to true (required for --add-exports)");
        }
    }

    private void configureCompilerArgs(Xpp3Dom config) {
        Xpp3Dom compilerArgs = config.getChild("compilerArgs");
        if (compilerArgs == null) {
            compilerArgs = new Xpp3Dom("compilerArgs");
            config.addChild(compilerArgs);
        }

        for (String requiredArg : REQUIRED_COMPILER_ARGS) {
            if (!containsArg(compilerArgs, requiredArg)) {
                Xpp3Dom arg = new Xpp3Dom("arg");
                arg.setValue(requiredArg);
                compilerArgs.addChild(arg);
                getLog().debug("FluentJava: added compilerArg: " + requiredArg);
            }
        }
    }

    private void configureAnnotationProcessorPaths(Xpp3Dom config) {
        Xpp3Dom paths = config.getChild("annotationProcessorPaths");
        if (paths == null) {
            paths = new Xpp3Dom("annotationProcessorPaths");
            config.addChild(paths);
        }

        if (!containsProcessorPath(paths, "fluentjava-plugin")) {
            Xpp3Dom path = new Xpp3Dom("path");

            Xpp3Dom groupId = new Xpp3Dom("groupId");
            groupId.setValue("io.fluentjava");
            path.addChild(groupId);

            Xpp3Dom artifactId = new Xpp3Dom("artifactId");
            artifactId.setValue("fluentjava-plugin");
            path.addChild(artifactId);

            Xpp3Dom version = new Xpp3Dom("version");
            version.setValue(project.getVersion());
            path.addChild(version);

            paths.addChild(path);
            getLog().debug("FluentJava: added fluentjava-plugin to annotationProcessorPaths");
        }
    }

    private boolean containsArg(Xpp3Dom compilerArgs, String value) {
        for (Xpp3Dom child : compilerArgs.getChildren()) {
            if (value.equals(child.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsProcessorPath(Xpp3Dom paths, String artifactId) {
        for (Xpp3Dom child : paths.getChildren()) {
            Xpp3Dom aid = child.getChild("artifactId");
            if (aid != null && artifactId.equals(aid.getValue())) {
                return true;
            }
        }
        return false;
    }
}
