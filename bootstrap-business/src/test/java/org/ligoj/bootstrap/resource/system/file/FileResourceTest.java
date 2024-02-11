/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.file;

import jakarta.ws.rs.ForbiddenException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Test class of {@link FileResource}
 */
@ExtendWith(SpringExtension.class)
class FileResourceTest extends AbstractBootTest {

	@Autowired
	private FileResource resource;
	@Autowired
	private ConfigurationResource configurationResource;

	@Test
	void upload() throws IOException {
		configurationResource.put("ligoj.file.path", ".*");

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


	@Test
	void uploadNotAllowed() throws IOException {
		configurationResource.put("ligoj.file.path", "echo");
		final var upload = new File(".tmp/orm-upload.xml");
		final var input = new ClassPathResource("META-INF/orm.xml").getInputStream();
		Assertions.assertThrows(ForbiddenException.class, ()->resource.upload(input, upload.getAbsolutePath(), "False"));
		Assertions.assertThrows(ForbiddenException.class, ()->resource.download(upload.getAbsolutePath()));
		Assertions.assertThrows(ForbiddenException.class, ()->resource.delete(upload.getAbsolutePath()));
	}

}
