/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.resource.BusinessException;

/**
 * Exception mapper test using {@link BusinessExceptionMapper}
 */
class BusinessExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new BusinessException(BusinessException.KEY_UNKNOWN_ID, new IOException(), "parameter1", "parameter2");
		check(mock(new BusinessExceptionMapper()).toResponse(exception), 400,
				"{\"code\":\"business\",\"message\":\"unknown-id\",\"parameters\":[\"parameter1\",\"parameter2\"],\"cause\":null}");
	}

}
