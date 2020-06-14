/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of {@link HSQLDialect25}
 */
class HSQLDialect25Test {

	@SuppressWarnings("deprecation")
	@Test
	void constr() throws IOException {
		Assertions.assertTrue(new HSQLDialect25().getKeywords().contains("period"));
	}

}
