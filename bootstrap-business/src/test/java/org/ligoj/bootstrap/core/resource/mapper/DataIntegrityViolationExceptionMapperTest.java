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

	@Test
	void toResponseForeignPostgreSql() {
		final var exception = new DataIntegrityViolationException("", new IllegalStateException(
				"ERROR: update or delete on table \"project\" violates foreign key constraint \"fk_assignment_project\" on table \"assignment\"\n"
						+ "  Detail: Key (id)=(5) is still referenced from table \"assignment\"."));
		check(mock(new DataIntegrityViolationExceptionMapper()).toResponse(exception), 412,
				"{\"code\":\"integrity-foreign\",\"message\":\"assignment/id\",\"parameters\":null,\"cause\":null}");
	}

	@Test
	void toResponseIntegrityPostgreSql() {
		final var exception = new DataIntegrityViolationException("", new IllegalStateException(
				"ERROR: duplicate key value violates unique constraint \"uk_s836pm716bbd4dqf5ddcv1ebt\"\n"
						+ "  Detail: Key (parameter, node)=(service:prov:aws:access-key-id, service:prov:aws:test) already exists."));
		check(mock(new DataIntegrityViolationExceptionMapper()).toResponse(exception), 412,
				"{\"code\":\"integrity-unicity\",\"message\":\"service:prov:aws:access-key-id, service:prov:aws:test/parameter, node\",\"parameters\":null,\"cause\":null}");
	}

}
