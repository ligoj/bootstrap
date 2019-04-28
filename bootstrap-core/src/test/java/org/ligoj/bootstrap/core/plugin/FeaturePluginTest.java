/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link FeaturePlugin}
 */
class FeaturePluginTest {

	private final org.ligoj.bootstrap.core.plugin.FeaturePlugin plugin = () -> "service:s1:t2";

	@Test
    void getName() {
		Assertions.assertEquals("T2", plugin.getName());
	}

	@Test
    void getVendor() {
		Assertions.assertNull(plugin.getVendor());
	}

	@Test
    void getVersion() {
		Assertions.assertNull(plugin.getVersion());
	}

	@Test
    void getInstalledEntities() {
		Assertions.assertTrue(plugin.getInstalledEntities().isEmpty());
	}

	@Test
    void install() throws Exception {
		// Nothing done there
		plugin.install();
	}

	@Test
    void update() throws Exception {
		// Nothing done there
		plugin.update("any");
	}

	@Test
    void compareTo() {
		Assertions.assertEquals(0, plugin.compareTo(plugin));
	}

}
