/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Test {@link AuditorStringAwareImpl}
 */
class AuditorStringAwareImplTest {

	private AuditorStringAwareImpl auditor;

	@BeforeEach
    void setup() {
		auditor = new AuditorStringAwareImpl();
		auditor.setSecurityHelper(new SecurityHelper());
		SecurityContextHolder.clearContext();
	}

	@Test
    void getCurrentAuditorNoUser() {
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, auditor.getCurrentAuditor().get());
	}

	@Test
    void getCurrentAuditorNoProvider() {
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, new AuditorStringAwareImpl().getCurrentAuditor().get());
	}

	@Test
    void getCurrentAuditor() {
		new SecurityHelper().setUserName("name");
		Assertions.assertEquals("name", auditor.getCurrentAuditor().get());
	}

}