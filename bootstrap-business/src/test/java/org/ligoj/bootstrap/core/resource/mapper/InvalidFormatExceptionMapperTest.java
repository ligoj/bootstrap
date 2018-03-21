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
public class InvalidFormatExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final InvalidFormatException exception = new InvalidFormatException(null, "", "", String.class);
		check(mock(new InvalidFormatExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

}
