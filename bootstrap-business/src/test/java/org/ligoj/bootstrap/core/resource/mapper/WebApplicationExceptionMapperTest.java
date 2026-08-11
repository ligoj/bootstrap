/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link WebApplicationExceptionMapper}
 */
class WebApplicationExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new WebApplicationException("message-error", 200);
		check(mock(new WebApplicationExceptionMapper()).toResponse(exception), 200, "{\"code\":\"internal\"}");
	}

	@Test
	void toResponse404() {
		final var exception = new WebApplicationException("message-error", 404);
		check(mock(new WebApplicationExceptionMapper()).toResponse(exception), 404, "{\"code\":\"internal\"}");
	}

	@Test
	void toResponse405() {
		final var exception = new WebApplicationException("message-error", 405);
		check(mock(new WebApplicationExceptionMapper()).toResponse(exception), 405, "{\"code\":\"internal\"}");
	}
}
