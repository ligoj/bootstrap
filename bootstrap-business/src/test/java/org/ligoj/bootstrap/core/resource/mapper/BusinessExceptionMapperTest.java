package org.ligoj.bootstrap.core.resource.mapper;

import java.io.IOException;

import org.junit.Test;
import org.ligoj.bootstrap.core.resource.AbstractMapperTest;
import org.ligoj.bootstrap.core.resource.BusinessException;

/**
 * Exception mapper test using {@link BusinessExceptionMapper}
 */
public class BusinessExceptionMapperTest extends AbstractMapperTest {

	@Test
	public void toResponse() {
		final BusinessException exception = new BusinessException(BusinessException.KEY_UNKNOW_ID, new IOException(), "parameter1", "parameter2");
		check(mock(new BusinessExceptionMapper()).toResponse(exception), 500,
				"{\"code\":\"business\",\"message\":\"unknown-id\",\"parameters\":[\"parameter1\",\"parameter2\"],\"cause\":null}");
	}

}
