package org.ligoj.bootstrap.core.security;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Test {@link AuditorStringAwareImpl}
 */
public class AuditorStringAwareImplTest {

	private AuditorStringAwareImpl auditor;

	@BeforeEach
	public void setup() {
		auditor = new AuditorStringAwareImpl();
		auditor.setSecurityHelper(new SecurityHelper());
		SecurityContextHolder.clearContext();
	}

	@Test
	public void getCurrentAuditorNoUser() {
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, auditor.getCurrentAuditor().get());
	}

	@Test
	public void getCurrentAuditorNoProvider() {
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, new AuditorStringAwareImpl().getCurrentAuditor().get());
	}

	@Test
	public void getCurrentAuditor() {
		new SecurityHelper().setUserName("name");
		Assertions.assertEquals("name", auditor.getCurrentAuditor().get());
	}

}