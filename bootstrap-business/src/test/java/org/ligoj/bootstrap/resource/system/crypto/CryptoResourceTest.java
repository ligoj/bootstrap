/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.crypto;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.resource.system.security.CryptoResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link CryptoResource}
 */
@ExtendWith(SpringExtension.class)
class CryptoResourceTest extends AbstractBootTest {

	@Autowired
	private CryptoResource resource;

	@Autowired
	private CryptoHelper cryptoHelper;

	@BeforeAll
    static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

	@AfterAll
    static void clean() {
		System.clearProperty("app.crypto.file");
	}

	@BeforeEach
	@AfterEach
	void reset() {
		System.clearProperty("test.property");
	}

	/**
	 * test find all service
	 */
	@Test
	void testFindAll() {
		final var encrypted = resource.create("value");
		Assertions.assertNotEquals(encrypted, "value");
		Assertions.assertEquals("value", cryptoHelper.decrypt(encrypted));

	}
}
