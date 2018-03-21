/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * Exception mapper test using {@link DataAccessResourceFailureExceptionMapper}
 */
public class DataAccessResourceFailureExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final CannotGetJdbcConnectionException exception = new CannotGetJdbcConnectionException("message", new SQLException());
		check(mock(new DataAccessResourceFailureExceptionMapper()).toResponse(exception), 503,
				"{\"code\":\"database-down\",\"message\":null,\"parameters\":null,\"cause\":null}");
	}

}
