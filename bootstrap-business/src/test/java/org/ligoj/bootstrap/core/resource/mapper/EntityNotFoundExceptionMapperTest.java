package org.ligoj.bootstrap.core.resource.mapper;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;

/**
 * Exception mapper test using {@link EntityNotFoundExceptionMapper}
 */
public class EntityNotFoundExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final EntityNotFoundException exception = new EntityNotFoundException("message-error");
		check(mock(new EntityNotFoundExceptionMapper()).toResponse(exception), 404,
				"{\"code\":\"entity\",\"message\":\"message-error\",\"parameters\":null,\"cause\":null}");
	}

}
