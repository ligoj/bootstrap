/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test class of {@link ISystemUserDetailsProvider}
 */
class ISystemUserDetailsProviderTest {

	@Test
	void decorateDefault() {
		// The default decoration is a no-op
		final ISystemUserDetailsProvider provider = (criteria, page) -> null;
		Assertions.assertDoesNotThrow(() -> provider.decorate(List.of()));
		Assertions.assertNull(provider.findAll("any", null));
	}
}
