/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * Exception mapper test using {@link CannotCreateTransactionExceptionMapper}
 */
class CannotCreateTransactionExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new CannotCreateTransactionException("message-error");
		check(mock(new CannotCreateTransactionExceptionMapper()).toResponse(exception), 503,
				"{\"code\":\"database-down\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

}
