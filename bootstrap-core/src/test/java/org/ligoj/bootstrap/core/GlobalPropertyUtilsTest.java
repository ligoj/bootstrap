/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;

/**
 * Check Spring resource loader. Test class of {@link GlobalPropertyUtils}
 */
class GlobalPropertyUtilsTest {

	/**
	 * No provided locations.
	 * 
	 * @throws IOException
	 *             Read issue occurred.
	 */
	@Test
    void testNoLocations() throws IOException {
		new GlobalPropertyUtils().setLocations(new Resource[0]);
		new GlobalPropertyUtils().loadProperties(new Properties());
		Assertions.assertNull(GlobalPropertyUtils.getProperty("key"));
	}

	/**
	 * Location does not exist.
	 * 
	 * @throws IOException
	 *             Read issue occurred.
	 */
	@Test
    void testLocationNoInput() throws IOException {
		final var resources = new Resource[] { Mockito.mock(Resource.class) };
		Mockito.when(resources[0].getInputStream()).thenReturn(null);
		new GlobalPropertyUtils().setLocations(resources);
		new GlobalPropertyUtils().loadProperties(new Properties());
		Assertions.assertNull(GlobalPropertyUtils.getProperty("key"));
	}

	/**
	 * Resource read causes error.
	 * 
	 * @throws IOException
	 *             Read issue occurred.
	 */
	@Test
    void testLocationInputError() throws IOException {
		final var resources = new Resource[1];
		final var resource = Mockito.mock(Resource.class);
		resources[0] = resource;
		Mockito.doThrow(new IOException()).when(resource).getInputStream();
		new GlobalPropertyUtils().setLocations(resources);
	}

	/**
	 * Resource read causes error.
	 * 
	 * @throws IOException
	 *             Read issue occurred.
	 */
	@Test
    void testLocationInputError2() throws IOException {
		final var resources = new Resource[1];
		final var resource = Mockito.mock(Resource.class);
		resources[0] = resource;
		Mockito.doThrow(new IllegalStateException()).when(resource).getInputStream();
		Assertions.assertThrows(IllegalStateException.class, () -> new GlobalPropertyUtils().setLocations(resources));
	}

	/**
	 * Full resource usage.
	 * 
	 * @throws IOException
	 *             Read issue occurred.
	 */
	@Test
    void testLocation() throws IOException {
		final var resources = new Resource[1];
		final var resource = Mockito.mock(Resource.class);
		final InputStream input = new ByteArrayInputStream("key=value".getBytes());
		Mockito.when(resource.getInputStream()).thenReturn(input);
		resources[0] = resource;
		new GlobalPropertyUtils().setLocations(resources);
		new GlobalPropertyUtils().loadProperties(new Properties());
		Assertions.assertEquals("value", GlobalPropertyUtils.getProperty("key"));
	}
}
