/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Exception mapper test using {@link UnrecognizedPropertyExceptionMapper}
 */
class UnrecognizedPropertyExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new UnrecognizedPropertyException(null, "", null, String.class, "property",
				Collections.emptyList());
		exception.prependPath(null, "property");
		exception.prependPath("property", "property2");
		check(mock(new UnrecognizedPropertyExceptionMapper()).toResponse(exception), 400,
				"{\"errors\":{\"property2.property\":[{\"rule\":\"Mapping\"}]}}");
	}

}
