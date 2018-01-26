package org.ligoj.bootstrap.core.resource.mapper;

import java.util.Collections;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link Jsr303ExceptionMapper}
 */
public class Jsr303ExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final ConstraintViolationException exception = new ConstraintViolationException(Collections.emptySet());
		check(mock(new Jsr303ExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

}
