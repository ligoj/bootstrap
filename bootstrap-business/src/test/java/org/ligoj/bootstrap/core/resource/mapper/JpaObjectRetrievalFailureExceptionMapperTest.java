/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

/**
 * Exception mapper test using {@link JpaObjectRetrievalFailureExceptionMapper}
 */
class JpaObjectRetrievalFailureExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new JpaObjectRetrievalFailureException(new EntityNotFoundException("key"));
		check(mock(new JpaObjectRetrievalFailureExceptionMapper()).toResponse(exception), 404,
				"{\"code\":\"entity\",\"message\":\"key\",\"parameters\":null,\"cause\":null}");
	}
}
