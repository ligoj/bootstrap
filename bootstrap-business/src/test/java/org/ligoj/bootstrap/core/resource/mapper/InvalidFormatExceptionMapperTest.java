/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Exception mapper test using {@link InvalidFormatExceptionMapper}
 */
class InvalidFormatExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new InvalidFormatException(null, "", "", String.class);
		check(mock(new InvalidFormatExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

}
