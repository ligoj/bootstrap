package org.ligoj.bootstrap.core.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link ServerError}
 */
public class ServerErrorTest {

	@Test
	public void throwableNull() {
		final ServerError serverError = new ServerError();
		serverError.setThrowable(new NullPointerException());
		Assertions.assertNull(serverError.getCause());
		Assertions.assertNull(serverError.getMessage());
	}

	@Test
	public void throwableNullMessage() {
		final ServerError serverError = new ServerError();
		serverError.setThrowable(new TechnicalException("message", new NullPointerException()));
		Assertions.assertNull(serverError.getCause());
		Assertions.assertEquals("message", serverError.getMessage());
	}

	@Test
	public void throwableCauseMessage() {
		final ServerError serverError = new ServerError();
		final TechnicalException exception = new TechnicalException("message2");
		serverError.setThrowable(new TechnicalException("message1", exception));
		Assertions.assertNull(serverError.getCause().getCause());
		Assertions.assertEquals("message2", serverError.getCause().getMessage());
		Assertions.assertEquals("message1", serverError.getMessage());
	}
}
