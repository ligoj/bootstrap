/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.mfa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Test class of {@link AaguidRegistry}.
 */
class AaguidRegistryTest {

	@Test
	void bundled() {
		Assertions.assertTrue(AaguidRegistry.size() > 0);
		Assertions.assertNull(AaguidRegistry.model(null));
		Assertions.assertNull(AaguidRegistry.model("00000000-0000-0000-0000-000000000000"));
	}

	@Test
	void load() {
		final var models = AaguidRegistry.load(new ByteArrayInputStream("ABC = Model \n".getBytes(StandardCharsets.UTF_8)));
		Assertions.assertEquals("Model", models.get("abc"));
	}

	@Test
	void loadMissing() {
		Assertions.assertTrue(AaguidRegistry.load(null).isEmpty());
	}

	@Test
	void loadError() {
		final var failing = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("Simulated");
			}
		};
		Assertions.assertTrue(AaguidRegistry.load(failing).isEmpty());
	}
}
