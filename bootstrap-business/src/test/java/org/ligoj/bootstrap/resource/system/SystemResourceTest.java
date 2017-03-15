package org.ligoj.bootstrap.resource.system;

import java.util.TimeZone;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.ligoj.bootstrap.core.DateUtils;

/**
 * Test class of {@link SystemResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class SystemResourceTest {

	@Autowired
	private SystemResource resource;

	@Test
	public void getConfiguration() {
		final SystemVo systemVo = resource.getConfiguration();
		Assert.assertTrue(systemVo.getCpu().getTotal() > 0);
		Assert.assertTrue(systemVo.getMemory().getFreeMemory() > 0);
		Assert.assertTrue(systemVo.getMemory().getMaxMemory() > 0);
		Assert.assertTrue(systemVo.getMemory().getTotalMemory() > 0);
		Assert.assertEquals(TimeZone.getDefault().getID(), systemVo.getDate().getDefaultTimeZone());
		Assert.assertEquals(TimeZone.getDefault().getID(), systemVo.getDate().getOriginalDefaultTimeZone());
		Assert.assertEquals(DateUtils.getApplicationTimeZone().getID(), systemVo.getDate().getTimeZone());
		Assert.assertTrue(System.currentTimeMillis() >= systemVo.getDate().getDate().getTime());
		Assert.assertTrue(System.currentTimeMillis() - 1000 < systemVo.getDate().getDate().getTime());
		Assert.assertTrue(systemVo.getFiles().size() > 0);
		Assert.assertTrue(systemVo.getFiles().get(0).getFreeSpace() > 0);
		Assert.assertTrue(systemVo.getFiles().get(0).getTotalSpace() > 0);
		Assert.assertTrue(systemVo.getFiles().get(0).getUsableSpace() > 0);
		Assert.assertTrue(systemVo.getFiles().get(0).getAbsolutePath().length() > 0);
	}

	@Test
	public void setApplicationTimeZone() {
		TimeZone timeZone = DateUtils.getApplicationTimeZone();
		try {
			resource.setApplicationTimeZone("GMT");
			Assert.assertEquals("GMT", DateUtils.getApplicationTimeZone().getID());
		} finally {
			DateUtils.setApplicationTimeZone(timeZone);
		}
	}

	@Test
	public void setTimeZone() {
		TimeZone timeZone = TimeZone.getDefault();
		try {
			resource.setTimeZone("GMT");
			Assert.assertEquals("GMT", TimeZone.getDefault().getID());
		} finally {
			TimeZone.setDefault(timeZone);
		}
	}
}
