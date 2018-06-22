/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link FeaturePlugin}
 */
public class FeaturePluginTest {

	private final FeaturePlugin plugin = new FeaturePlugin() {

		@Override
		public String getKey() {
			return "key";
		}
	};

	@Test
	public void getInstalledEntities() {
		Assertions.assertTrue(plugin.getInstalledEntities().isEmpty());
	}

	@Test
	public void getName() {
		Assertions.assertEquals("Key", plugin.getName());
	}

	@Test
	public void getVendor() {
		Assertions.assertNull(plugin.getVendor());
	}

	@Test
	public void getVersion() {
		Assertions.assertNull(plugin.getVersion());
	}

}
