/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

/**
 * Exception mapper test using {@link ValidationJsonExceptionMapper}
 */
class ValidationJsonExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new ValidationJsonException("message-error");
		check(mock(new ValidationJsonExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

}
