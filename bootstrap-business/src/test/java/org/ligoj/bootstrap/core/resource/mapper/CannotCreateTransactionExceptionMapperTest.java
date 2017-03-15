package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * Exception mapper test using {@link CannotCreateTransactionExceptionMapper}
 */
public class CannotCreateTransactionExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final CannotCreateTransactionException exception = new CannotCreateTransactionException("message-error");
		check(mock(new CannotCreateTransactionExceptionMapper()).toResponse(exception), 503,
				"{\"code\":\"database-down\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

}
