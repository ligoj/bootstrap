package org.ligoj.bootstrap.core.security;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Test {@link AuditorStringAwareImpl}
 */
public class AuditorStringAwareImplTest {

	private AuditorStringAwareImpl auditor;

	@Before
	public void setup() {
		auditor = new AuditorStringAwareImpl();
		auditor.setSecurityHelper(new SecurityHelper());
		SecurityContextHolder.clearContext();
	}

	@Test
	public void getCurrentAuditorNoUser() {
		Assert.assertEquals(SecurityHelper.SYSTEM_USERNAME, auditor.getCurrentAuditor().get());
	}

	@Test
	public void getCurrentAuditorNoProvider() {
		Assert.assertEquals(SecurityHelper.SYSTEM_USERNAME, new AuditorStringAwareImpl().getCurrentAuditor().get());
	}

	@Test
	public void getCurrentAuditor() {
		new SecurityHelper().setUserName("name");
		Assert.assertEquals("name", auditor.getCurrentAuditor().get());
	}

}