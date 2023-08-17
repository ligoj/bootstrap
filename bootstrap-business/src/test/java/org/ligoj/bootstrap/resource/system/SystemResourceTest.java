/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system;

import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.DateUtils;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link SystemResource}
 */
@ExtendWith(SpringExtension.class)
class SystemResourceTest extends AbstractBootTest {

	@Autowired
	private SystemResource resource;

	@Test
	void getConfiguration() {
		final var systemVo = resource.getConfiguration();
		Assertions.assertTrue(systemVo.getCpu().getTotal() > 0);
		Assertions.assertTrue(systemVo.getMemory().getFreeMemory() > 0);
		Assertions.assertTrue(systemVo.getMemory().getMaxMemory() > 0);
		Assertions.assertTrue(systemVo.getMemory().getTotalMemory() > 0);
		Assertions.assertEquals(TimeZone.getDefault().getID(), systemVo.getDate().getDefaultTimeZone());
		Assertions.assertEquals(TimeZone.getDefault().getID(), systemVo.getDate().getOriginalDefaultTimeZone());
		Assertions.assertEquals(DateUtils.getApplicationTimeZone().getID(), systemVo.getDate().getTimeZone());
		Assertions.assertTrue(System.currentTimeMillis() >= systemVo.getDate().getDate().getTime());
		Assertions.assertTrue(System.currentTimeMillis() - 1000 < systemVo.getDate().getDate().getTime());
		Assertions.assertFalse(systemVo.getFiles().isEmpty());
		Assertions.assertTrue(systemVo.getFiles().get(0).getFreeSpace() > 0);
		Assertions.assertTrue(systemVo.getFiles().get(0).getTotalSpace() > 0);
		Assertions.assertTrue(systemVo.getFiles().get(0).getUsableSpace() > 0);
		Assertions.assertFalse(systemVo.getFiles().get(0).getAbsolutePath().isEmpty());
	}

	@Test
	void setApplicationTimeZone() {
        var timeZone = DateUtils.getApplicationTimeZone();
		try {
			resource.setApplicationTimeZone("GMT");
			Assertions.assertEquals("GMT", DateUtils.getApplicationTimeZone().getID());
		} finally {
			DateUtils.setApplicationTimeZone(timeZone);
		}
	}

	@Test
	void setTimeZone() {
        var timeZone = TimeZone.getDefault();
		try {
			resource.setTimeZone("GMT");
			Assertions.assertEquals("GMT", TimeZone.getDefault().getID());
		} finally {
			TimeZone.setDefault(timeZone);
		}
	}
}
