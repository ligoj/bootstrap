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
public class MismatchedInputExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final MismatchedInputException exception = MismatchedInputException.from(null, String.class, "msg");
		check(mock(new MismatchedInputExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

}
