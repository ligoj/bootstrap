/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link PluginException}
 */
class PluginExceptionTest {

	@Test
    void getInstalledEntities() {
		Assertions.assertEquals("plugin", new PluginException("plugin", "message").getPlugin());
	}

}
