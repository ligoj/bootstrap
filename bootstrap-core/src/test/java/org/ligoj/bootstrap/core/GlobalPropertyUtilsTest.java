/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * Check Spring resource loader. Test class of {@link GlobalPropertyUtils}
 */
class GlobalPropertyUtilsTest {

	/**
	 * No provided locations.
	 */
	@Test
	void testNoLocations() {
		Assertions.assertNull(GlobalPropertyUtils.getProperty("any"));
	}

	/**
	 * Location does not exist.
	 *
	 * @throws IOException Read issue occurred.
	 */
	@Test
	void testLocationNoInput() throws IOException {
		final var resources = new Resource[]{mock(Resource.class)};
		when(resources[0].getInputStream()).thenReturn(null);
		new GlobalPropertyUtils().setLocations(resources);

		// Not error expected
		new GlobalPropertyUtils().loadProperties(new Properties());
	}

	/**
	 * Resource read causes error.
	 *
	 * @throws IOException Read issue occurred.
	 */
	@Test
	void testLocationInputError() throws IOException {
		final var resources = new Resource[1];
		final var resource = mock(Resource.class);
		resources[0] = resource;
		doThrow(new IOException()).when(resource).getInputStream();
		new GlobalPropertyUtils().setLocations(resources);
	}

	/**
	 * Resource read causes error.
	 *
	 * @throws IOException Read issue occurred.
	 */
	@Test
	void testLocationInputError2() throws IOException {
		final var resources = new Resource[1];
		final var resource = mock(Resource.class);
		resources[0] = resource;
		doThrow(new IllegalStateException()).when(resource).getInputStream();
		final var utils = new GlobalPropertyUtils();
		Assertions.assertThrows(IllegalStateException.class, () -> utils.setLocations(resources));
	}

	/**
	 * Full resource usage.
	 *
	 * @throws IOException Read issue occurred.
	 */
	@Test
	void testLocation() throws IOException {
		final var resources = new Resource[1];
		final var resource = mock(Resource.class);
		final InputStream input = new ByteArrayInputStream("key=value".getBytes());
		when(resource.getInputStream()).thenReturn(input);
		resources[0] = resource;
		new GlobalPropertyUtils().setLocations(resources);
		new GlobalPropertyUtils().loadProperties(new Properties());
		Assertions.assertEquals("value", GlobalPropertyUtils.getProperty("key"));
	}
}
