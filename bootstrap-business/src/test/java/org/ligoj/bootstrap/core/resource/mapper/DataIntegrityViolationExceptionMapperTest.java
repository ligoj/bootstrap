/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Exception mapper test using {@link DataIntegrityViolationExceptionMapper}
 */
class DataIntegrityViolationExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponseForeign() {
		final var exception = new DataIntegrityViolationException("",
				new IllegalStateException("bla `assignment`, CONSTRAINT `FK3D2B86CDAF555D0B` FOREIGN KEY (`project`) bla"));
		check(mock(new DataIntegrityViolationExceptionMapper()).toResponse(exception), 412,
				"{\"code\":\"integrity-foreign\",\"message\":\"assignment/project\",\"parameters\":null,\"cause\":null}");
	}

	@Test
	void toResponseIntegrity() {
		final var exception = new DataIntegrityViolationException("",
				new IllegalStateException("Duplicate entry '2003' for key 'PRIMARY'"));
		check(mock(new DataIntegrityViolationExceptionMapper()).toResponse(exception), 412,
				"{\"code\":\"integrity-unicity\",\"message\":\"2003/PRIMARY\",\"parameters\":null,\"cause\":null}");
	}

	@Test
	void toResponse() {
		final var exception = new DataIntegrityViolationException("", new IllegalStateException("Any SQL error"));
		check(mock(new DataIntegrityViolationExceptionMapper()).toResponse(exception), 412,
				"{\"code\":\"integrity-unknown\",\"message\":\"Any SQL error\",\"parameters\":null,\"cause\":null}");
	}

}
