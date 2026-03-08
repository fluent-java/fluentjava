package io.fluentjava.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

/**
 * Gradle plugin that automatically configures the Java compiler for FluentJava.
 *
 * <p>Injects the required compiler arguments, fork mode, and dependencies
 * so that users only need to apply the plugin and add the runtime dependency.</p>
 *
 * <h3>Groovy DSL:</h3>
 * <pre>{@code
 * plugins {
 *     id 'java'
 *     id 'io.fluentjava' version '1.0.0'
 * }
 *
 * dependencies {
 *     implementation 'io.fluentjava:fluentjava-runtime:1.0.0'
 * }
 * }</pre>
 *
 * <h3>Kotlin DSL:</h3>
 * <pre>{@code
 * plugins {
 *     java
 *     id("io.fluentjava") version "1.0.0"
 * }
 *
 * dependencies {
 *     implementation("io.fluentjava:fluentjava-runtime:1.0.0")
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class FluentJavaPlugin implements Plugin<Project> {

    private static final String FLUENTJAVA_VERSION = "1.0.0";

    private static final List<String> COMPILER_ARGS = List.of(
            "-Xplugin:FluentJava",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"
    );

    @Override
    public void apply(Project project) {
        // Ensure java plugin is applied
        project.getPlugins().apply(JavaPlugin.class);

        // Add fluentjava-plugin as compileOnly + annotationProcessor
        project.getDependencies().add("compileOnly",
                "io.fluentjava:fluentjava-plugin:" + FLUENTJAVA_VERSION);
        project.getDependencies().add("annotationProcessor",
                "io.fluentjava:fluentjava-plugin:" + FLUENTJAVA_VERSION);

        // Configure all JavaCompile tasks
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.getOptions().setFork(true);
            List<String> existing = task.getOptions().getCompilerArgs();
            for (String arg : COMPILER_ARGS) {
                if (!existing.contains(arg)) {
                    existing.add(arg);
                }
            }
        });

        project.getLogger().lifecycle("FluentJava: compiler configured automatically.");
    }
}
