/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

/**
 * Exception mapper test using {@link ValidationJsonExceptionMapper}
 */
class ValidationJsonExceptionMapperTest extends AbstractMapperTest {

	@Test
	void toResponse() {
		final var exception = new ValidationJsonException("message-error");
		check(mock(new ValidationJsonExceptionMapper()).toResponse(exception), 400, "{\"errors\":{}}");
	}

	@Test
	void toResponseCause() {
		final var exception = new ValidationJsonException("property1", "message-error", "key", "value");
		exception.setStackTrace(new StackTraceElement[]{new StackTraceElement("classLoaderName",
				"moduleName", "moduleVersion",
				"declaringClass", "methodName",
				"fileName", 1)});
		exception.initCause(new ValidationJsonException("property2", "message-error2", "key", "value"));
		check(mock(new ValidationJsonExceptionMapper()).toResponse(exception), 400, "{\"errors\":{\"property1\":[{\"rule\":\"message-error\",\"parameters\":{\"key\":\"value\"}}]}}");
	}

	@Test
	void toResponseCauseForkedInjected() {
		final var exception = new ValidationJsonException("property1", "message-error", "key", "value");
		exception.setStackTrace(new StackTraceElement[]{new StackTraceElement("classLoaderName",
				"moduleName", "moduleVersion",
				"declaringClass", "newInstance",
				"fileName", 1)});
		exception.initCause(new ValidationJsonException("property2", "message-error2", "key", "value"));
		check(mock(new ValidationJsonExceptionMapper()).toResponse(exception), 400, "{\"errors\":{\"property2\":[{\"rule\":\"message-error2\",\"parameters\":{\"key\":\"value\"}}]}}");
	}

}
