/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link EntityNotFoundExceptionMapper}
 */
class EntityNotFoundExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new EntityNotFoundException("message-error");
		check(mock(new EntityNotFoundExceptionMapper()).toResponse(exception), 404,
				"{\"code\":\"entity\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}

}
