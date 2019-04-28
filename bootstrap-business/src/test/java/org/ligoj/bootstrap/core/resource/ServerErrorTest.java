/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link ServerError}
 */
class ServerErrorTest {

	@Test
	void throwableNull() {
		final var serverError = new ServerError();
		serverError.setThrowable(new NullPointerException());
		Assertions.assertNull(serverError.getCause());
		Assertions.assertNull(serverError.getMessage());
	}

	@Test
	void throwableNullMessage() {
		final var serverError = new ServerError();
		serverError.setThrowable(new TechnicalException("message", new NullPointerException()));
		Assertions.assertNull(serverError.getCause());
		Assertions.assertEquals("message", serverError.getMessage());
	}

	@Test
	void throwableCauseMessage() {
		final var serverError = new ServerError();
		final var exception = new TechnicalException("message2");
		serverError.setThrowable(new TechnicalException("message1", exception));
		Assertions.assertNull(serverError.getCause().getCause());
		Assertions.assertEquals("message2", serverError.getCause().getMessage());
		Assertions.assertEquals("message1", serverError.getMessage());
	}
}
