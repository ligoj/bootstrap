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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

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


	/**
	 * Create a fresh "allowed" directory and authorize only its content.
	 */
	private Path prepareAllowed() throws IOException {
		final var base = Path.of("target/file-test").toAbsolutePath();
		FileUtils.deleteDirectory(base.toFile());
		final var allowed = Files.createDirectories(base.resolve("allowed")).toRealPath();
		configurationResource.put("ligoj.file.path", Pattern.quote(allowed.toString()) + "/.*");
		return allowed;
	}

	@Test
	void pathTraversal() throws IOException {
		final var allowed = prepareAllowed();
		final var outside = allowed.resolveSibling("secret.txt");
		Files.writeString(outside, "secret");

		// Matches the raw pattern, but resolves outside the allowed directory
		final var traversal = allowed + "/../secret.txt";
		final var input = new ByteArrayInputStream("evil".getBytes());
		Assertions.assertThrows(ForbiddenException.class, () -> resource.upload(input, traversal, "true"));
		Assertions.assertThrows(ForbiddenException.class, () -> resource.download(traversal));
		Assertions.assertThrows(ForbiddenException.class, () -> resource.delete(traversal));
		Assertions.assertEquals("secret", Files.readString(outside));
	}

	@Test
	void pathSymbolicLink() throws IOException {
		final var allowed = prepareAllowed();
		final var outside = Files.createDirectories(allowed.resolveSibling("outside"));
		Files.createSymbolicLink(allowed.resolve("link"), outside);

		// Inside the allowed directory by name, outside by the link target
		final var escaped = allowed + "/link/x.txt";
		final var input = new ByteArrayInputStream("evil".getBytes());
		Assertions.assertThrows(ForbiddenException.class, () -> resource.upload(input, escaped, "false"));
		Assertions.assertFalse(Files.exists(outside.resolve("x.txt")));
	}

	@Test
	void pathNormalizedInside() throws IOException {
		final var allowed = prepareAllowed();

		// A ".." staying inside the allowed directory is accepted, the normalized location is used
		resource.upload(new ByteArrayInputStream("ok".getBytes()), allowed + "/sub/../ok.txt", "false");
		Assertions.assertEquals("ok", Files.readString(allowed.resolve("ok.txt")));
		Assertions.assertFalse(Files.exists(allowed.resolve("sub")));
	}

	@Test
	void pathEmpty() {
		configurationResource.put("ligoj.file.path", ".*");
		Assertions.assertThrows(ForbiddenException.class, () -> resource.download(""));
		Assertions.assertThrows(ForbiddenException.class, () -> resource.download(null));
		Assertions.assertThrows(ForbiddenException.class, () -> resource.download("\0"));
	}

	@Test
	void uploadNotAllowed() throws IOException {
		configurationResource.put("ligoj.file.path", "echo");
		final var upload = new File(".tmp/orm-upload.xml").getAbsolutePath();
		final var input = new ClassPathResource("META-INF/orm.xml").getInputStream();
		Assertions.assertThrows(ForbiddenException.class, ()->resource.upload(input, upload, "False"));
		Assertions.assertThrows(ForbiddenException.class, ()->resource.download(upload));
		Assertions.assertThrows(ForbiddenException.class, ()->resource.delete(upload));
	}

}
