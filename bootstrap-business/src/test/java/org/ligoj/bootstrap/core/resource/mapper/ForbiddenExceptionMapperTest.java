/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.ForbiddenException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link ForbiddenExceptionMapper}
 */
class ForbiddenExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new ForbiddenException("message-error");
		check(mock(new ForbiddenExceptionMapper()).toResponse(exception), 403,
				"{\"code\":\"security\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

}
