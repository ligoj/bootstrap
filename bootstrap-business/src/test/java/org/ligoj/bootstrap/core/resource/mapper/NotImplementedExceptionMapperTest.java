/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link WebApplicationExceptionMapper}
 */
class NotImplementedExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new NotImplementedException("message-error");
		check(mock(new NotImplementedExceptionMapper()).toResponse(exception), 501,
				"{\"code\":\"not-implemented\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}
}
