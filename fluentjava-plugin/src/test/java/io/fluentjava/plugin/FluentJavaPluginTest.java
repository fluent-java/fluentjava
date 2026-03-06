package io.fluentjava.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FluentJavaPlugin}.
 */
@DisplayName("FluentJavaPlugin")
class FluentJavaPluginTest {

    @Test
    @DisplayName("plugin name is 'FluentJava'")
    void pluginName() {
        FluentJavaPlugin plugin = new FluentJavaPlugin();
        assertEquals("FluentJava", plugin.getName());
    }

    @Test
    @DisplayName("plugin name matches -Xplugin argument")
    void pluginNameForXplugin() {
        FluentJavaPlugin plugin = new FluentJavaPlugin();
        // The name returned must match what users pass as -Xplugin:FluentJava
        String expected = "FluentJava";
        assertEquals(expected, plugin.getName(),
                "Plugin name must match -Xplugin:" + expected);
    }
}
