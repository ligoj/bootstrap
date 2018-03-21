/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.util.Collections;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.transaction.TransactionSystemException;

/**
 * Exception mapper test using {@link TransactionSystemExceptionMapper}
 */
public class TransactionSystemExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final TransactionSystemException exception = new TransactionSystemException("message-error");
		check(mock(new TransactionSystemExceptionMapper()).toResponse(exception), 500,
				"{\"code\":\"technical\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}

	@Test
	public void toResponseConstraint() {
		final TransactionSystemException exception = new TransactionSystemException("message-error",
				new ConstraintViolationException("message-error", Collections.emptySet()));
		check(mock(new TransactionSystemExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

}
