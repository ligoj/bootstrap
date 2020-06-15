/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.scan.spi.ScanParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test for {@link ResourceScanner}
 */
class ResourceScannerTest {

	/**
	 * Simulate {@link IOException} for {@link ResourceScanner#getJarUrl(URL)}
	 */
	@Test
	void testInJarUrlIoException() {
		final ResourceScanner scanner = new ResourceScanner() {
			@Override
			protected URL getJarUrl(final URL ormUrl) throws MalformedURLException {
				throw new MalformedURLException();
			}

		};
		final var scan = newScanEnvironment();
		Assertions.assertThrows(IllegalStateException.class, () -> scanner.scan(scan, null, null));
	}

	/**
	 * Simulate {@link IOException} for {@link ResourceScanner#getOrmUrls()}
	 */
	@Test
	void testFilesInJarIoException() {
		final ResourceScanner scanner = new ResourceScanner() {
			@Override
			protected Enumeration<URL> getOrmUrls() throws IOException {
				throw new IOException();
			}

		};
		final var scan = newScanEnvironment();
		Assertions.assertThrows(IllegalStateException.class, () -> scanner.scan(scan, null, null));
	}

	private ScanEnvironment newScanEnvironment() {
		var environment = Mockito.mock(ScanEnvironment.class);
		List<URL> nonRootUrls = new ArrayList<>();
		Mockito.when(environment.getNonRootUrls()).thenReturn(nonRootUrls);
		return environment;
	}

	/**
	 * Simulate a non existing JAR entry inside a WAR entry.
	 */
	@Test
	void testFilesInJarInWar() {
		final ResourceScanner scanner = new ResourceScanner() {
			@Override
			protected Enumeration<URL> getOrmUrls() throws IOException {
				return Collections.enumeration(Arrays.asList(new URL[] {
						new URL("jar:file:/c://my.war!/WEB-INF/libs/my.jar!/com/mycompany/MyClass.class") }));
			}

		};
		Assertions.assertTrue(
				scanner.scan(newScanEnvironment(), Mockito.mock(ScanOptions.class), Mockito.mock(ScanParameters.class))
						.getLocatedMappingFiles().isEmpty());
	}

	/**
	 * Simulate a non existing JAR entry.
	 */
	@Test
	void testFilesInJar() {
		final ResourceScanner scanner = new ResourceScanner() {
			@Override
			protected Enumeration<URL> getOrmUrls() throws IOException {
				return Collections.enumeration(
						Arrays.asList(new URL[] { new URL("jar:file:/c://my.jar!/com/mycompany/MyClass.class") }));
			}

		};
		Assertions.assertTrue(
				scanner.scan(newScanEnvironment(), Mockito.mock(ScanOptions.class), Mockito.mock(ScanParameters.class))
						.getLocatedMappingFiles().isEmpty());
	}

	/**
	 * Standard class.
	 */
	@Test
	void testFilesInJarIoException2() {
		final var scanner = new ResourceScanner();
		var scan = scanner.scan(Mockito.mock(ScanEnvironment.class), Mockito.mock(ScanOptions.class),
				Mockito.mock(ScanParameters.class));
		Assertions.assertNotNull(scan);
	}

}
