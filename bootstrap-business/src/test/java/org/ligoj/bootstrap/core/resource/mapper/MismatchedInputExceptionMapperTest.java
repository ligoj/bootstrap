/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/**
 * Exception mapper test using {@link MismatchedInputExceptionMapper}
 */
class MismatchedInputExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = MismatchedInputException.from(null, String.class, "msg");
		check(mock(new MismatchedInputExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

}
