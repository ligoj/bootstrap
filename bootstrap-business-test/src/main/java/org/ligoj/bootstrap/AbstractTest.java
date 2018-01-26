package org.ligoj.bootstrap;

import java.security.Permission;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import lombok.extern.slf4j.Slf4j;

/**
 * Common test class.
 */
@Slf4j
public class AbstractTest { // NOPMD NOSONAR
	protected static final int MOCK_PORT = 8120;

	/**
	 * Original security manager.
	 */
	protected static final ThreadLocal<SecurityManager> SECURITY_MANAGER_THREAD = new ThreadLocal<>();

	/**
	 * Initialize mocks of this class.
	 */
	@BeforeEach
	public void injectMock() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Install a hook to prevent {@link System#exit(int)} to be executed.
	 */
	@BeforeAll
	public static void forbidSystemExitCall() {
		SECURITY_MANAGER_THREAD.set(new SecurityManager() {
			@Override
			public void checkPermission(final Permission permission) {
				if (permission.getName().startsWith("exitVM") && !"-local".equals(System.getProperty("app-env", "-local"))) {
					log.error("Something called exit within test executions",
							new IllegalStateException("Something called exit within test executions"));
				}
			}
		});
		System.setSecurityManager(SECURITY_MANAGER_THREAD.get());
	}

	/**
	 * Restore previous security manager replaced by the one installed by {@link #forbidSystemExitCall()}.
	 */
	@AfterAll
	public static void restoreSystemExitCall() {
		System.setSecurityManager(SECURITY_MANAGER_THREAD.get());
	}

}
