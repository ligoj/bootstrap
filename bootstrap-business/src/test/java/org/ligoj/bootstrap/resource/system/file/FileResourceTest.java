/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.file;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.hook.HookResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Test class of {@link FileResource}
 */
@ExtendWith(SpringExtension.class)
class FileResourceTest extends AbstractBootTest {

	@Autowired
	private FileResource resource;

	@Test
	void upload() throws IOException {
		final var upload = new File(".tmp/orm-upload.xml");
		resource.upload(new ClassPathResource("META-INF/orm.xml").getInputStream(), upload.getAbsolutePath(), "False");
		resource.upload(new ClassPathResource("META-INF/orm.xml").getInputStream(), upload.getAbsolutePath(), "False");
		Assertions.assertTrue(upload.exists());
		FileUtils.contentEquals(new File("META-INF/spring/orm.xml"), upload);
		final var download = new File(".tmp/orm-download.xml");
		try (final var out = new FileOutputStream(download)) {
			resource.download(upload.getAbsolutePath()).transferTo(out);
		}
		Assertions.assertTrue(download.exists());
		FileUtils.contentEquals(download, upload);
		resource.delete(upload.getAbsolutePath());
		Assertions.assertFalse(upload.exists());
	}
}
