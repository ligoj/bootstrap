package org.ligoj.bootstrap.core.resource.mapper;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * Exception mapper test using {@link NoResultExceptionMapper}
 */
public class NoResultExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final EmptyResultDataAccessException exception = new EmptyResultDataAccessException("message-error", 1,
				new NotImplementedException("message-error2"));
		check(mock(new NoResultExceptionMapper()).toResponse(exception), 404,
				"{\"code\":\"entity\",\"message\":\"message-error2\",\"parameters\":null,\"cause\":null}");
	}

	@Test
	public void toResponse2() {
		final EmptyResultDataAccessException exception = new EmptyResultDataAccessException("", 1, new NoResultException("message"));
		check(mock(new NoResultExceptionMapper()).toResponse(exception), 404,
				"{\"code\":\"entity\",\"message\":\"message\",\"parameters\":null,\"cause\":null}");
	}
}
