/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.io.Closeable;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test of {@link AbstractTest}
 */
public class TestAbstractTest extends AbstractTest {

	@Test
	public void testCloseQuietly() throws IOException {
		final Closeable mock = Mockito.mock(Closeable.class);
		super.closeQuietly(mock);
		Mockito.verify(mock, Mockito.atLeastOnce()).close();
	}

	@Test
	public void testCloseQuietlyClosed() throws IOException {
		final Closeable mock = Mockito.mock(Closeable.class);
		Mockito.doThrow(new IOException()).when(mock).close();
		super.closeQuietly(mock);
		Mockito.verify(mock, Mockito.atLeastOnce()).close();
	}

	@Test
	public void testCloseNull() {
		super.closeQuietly(null);
	}

	@Test
	public void testCheckPermission() {
		// only for coverage
		SECURITY_MANAGER_THREAD.get().checkPermission(newPermission("name"));
		final String oldValue = System.getProperty("app-env");
		try {
			System.setProperty("app-env", "-test");
			SECURITY_MANAGER_THREAD.get().checkPermission(newPermission("exitVM"));
			System.setProperty("app-env", "-local");
			SECURITY_MANAGER_THREAD.get().checkPermission(newPermission("exitVM"));
		} finally {
			if (oldValue == null) {
				System.clearProperty("app-env");
			} else {
				System.setProperty("app-env", oldValue);
			}

		}
	}

	private java.security.Permission newPermission(final String name) {
		return new java.security.Permission(name) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean implies(java.security.Permission permission) {
				return false;
			}

			@Override
			public boolean equals(Object obj) {
				return false;
			}

			@Override
			public int hashCode() {
				return 0;
			}

			@Override
			public String getActions() {
				return null;
			}
		};
	}

}
