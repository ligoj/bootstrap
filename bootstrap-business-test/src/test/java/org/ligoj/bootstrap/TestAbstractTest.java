package org.ligoj.bootstrap;

import org.junit.Test;

/**
 * Test of {@link AbstractTest}
 */
public class TestAbstractTest extends AbstractTest {

	@Test
	public <T> void testCheckPermission() {
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
