/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * Exception mapper test using {@link TechnicalExceptionMapper}
 */
class TechnicalExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new TechnicalException("message-error", "p", "v");
		check(mock(new TechnicalExceptionMapper()).toResponse(exception), 500,
				"{\"code\":\"technical\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}
}
