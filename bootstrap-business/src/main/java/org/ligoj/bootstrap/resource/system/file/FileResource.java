/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.file;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.util.Arrays;

/**
 * System file resource.
 */
@Path("/system/file")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class FileResource {

	/**
	 * Message of a rejected path.
	 */
	private static final String NOT_ALLOWED = "Path location is not within one of allowed ${ligoj.file.path} value";

	@Autowired
	private ConfigurationResource configurationResource;

	/**
	 * Download a remote file.
	 *
	 * @param path Source file path.
	 * @return Source content stream.
	 * @throws IOException When file cannot be downloaded.
	 */
	@GET
	public InputStream download(@QueryParam("path") String path) throws IOException {
		return Files.newInputStream(checkPath(path)); // NOSONAR - Checked against the 'ligoj.file.path' allowed locations
	}

	/**
	 * Return true when given path is allowed according to 'ligoj.file.path' values.
	 *
	 * @param path The canonical file path to download or upload.
	 * @return true when given path is allowed according to 'ligoj.file.path' values.
	 */
	private boolean isAllowedPath(final String path) {
		return Arrays.stream(configurationResource.get("ligoj.file.path", "^$").split(",")).anyMatch(path::matches);
	}

	/**
	 * Return the canonical form of the given path: absolute, without "." or ".." segment, and with the symbolic links
	 * of its deepest existing ancestor resolved. The remaining part does not exist yet, so contains no link.
	 *
	 * @param absolute The absolute and normalized file path, already matching the allowed locations.
	 * @return The canonical path.
	 * @throws IOException When a symbolic link cannot be resolved, such as a dangling one.
	 */
	private java.nio.file.Path toCanonicalPath(final java.nio.file.Path absolute) throws IOException {
		var existing = absolute;
		while (!Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
			// The root always exists
			existing = existing.getParent();
		}
		return existing.toRealPath().resolve(existing.relativize(absolute));
	}

	/**
	 * Check the path for download or upload against 'ligoj.file.path' values, and return its canonical form. The
	 * patterns are matched twice: first against the normalized path, before any access to the file system, so the
	 * existence of a file out of the allowed locations cannot be probed; then against the canonical path, so ".."
	 * segments and symbolic links cannot escape them.
	 *
	 * @param path The file path to download or upload.
	 * @return The canonical path to use for the file operation.
	 */
	private java.nio.file.Path checkPath(final String path) {
		if (StringUtils.isNotBlank(path)) {
			try {
				final var normalized = java.nio.file.Path.of(path).toAbsolutePath().normalize();
				if (isAllowedPath(normalized.toString())) {
					// Only now the file system is accessed
					final var canonical = toCanonicalPath(normalized);
					if (isAllowedPath(canonical.toString())) {
						return canonical;
					}
				}
			} catch (final IOException | InvalidPathException e) {
				log.info("Invalid file path {}: {}", path, e.getMessage());
			}
		}
		throw new ForbiddenException(NOT_ALLOWED);
	}

	/**
	 * Upload a file, replacing any existing one.
	 *
	 * @param content    Target file content.
	 * @param path       Target file path.
	 * @param executable Make the file executable when 'true'.
	 * @throws IOException When file cannot be written.
	 */
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void upload(@Multipart("content") final InputStream content, @Multipart("path") final String path, @Multipart("executable") final String executable) throws IOException {
		final var file = checkPath(path).toFile();

		// Write the content
		log.info("Upload file {}, executable:{}", file, executable);
		FileUtils.createParentDirectories(file);
		try (final var out = new FileOutputStream(file)) {
			content.transferTo(out);
		}
		file.setExecutable(BooleanUtils.toBoolean(executable));
	}

	/**
	 * Delete a file.
	 *
	 * @param path the file name to delete.
	 * @throws IOException When file cannot be deleted.
	 */
	@DELETE
	public void delete(@QueryParam("path") final String path) throws IOException {
		FileUtils.delete(checkPath(path).toFile()); // NOSONAR - Checked against the 'ligoj.file.path' allowed locations
	}
}
