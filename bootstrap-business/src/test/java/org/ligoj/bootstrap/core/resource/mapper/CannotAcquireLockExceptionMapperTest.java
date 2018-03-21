/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.dao.CannotAcquireLockException;

/**
 * Exception mapper test using {@link CannotAcquireLockExceptionMapper}
 */
public class CannotAcquireLockExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final CannotAcquireLockException exception = new CannotAcquireLockException("lock");
		check(mock(new CannotAcquireLockExceptionMapper()).toResponse(exception), 409,
				"{\"code\":\"database-lock\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}
}
