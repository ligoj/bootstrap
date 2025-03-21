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
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
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
		checkPath(path);
		return new FileInputStream(path);
	}

	/**
	 * Return true when given path is allowed according to 'ligoj.file.path' values.
	 *
	 * @param path The file path to download or upload.
	 * @return true when given path is allowed according to 'ligoj.file.path' values.
	 */
	private boolean isAllowedPath(final String path) {
		return Arrays.stream(configurationResource.get("ligoj.file.path", "^$").split(",")).anyMatch(path::matches);
	}

	/**
	 * Check the path for download or upload.
	 *
	 * @param path The file path to download or upload.
	 */
	private void checkPath(final String path) {
		if (!isAllowedPath(path)) {
			throw new ForbiddenException("Path location is not within one of allowed ${ligoj.file.path} value");
		}
	}

	/**
	 * Create a hook with file content.
	 *
	 * @param content    Target file content.
	 * @param path       Target file path.
	 * @param executable Make the file executable when 'true'.
	 * @throws IOException When file cannot be written.
	 */
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void upload(@Multipart("content") final InputStream content, @Multipart("path") final String path, @Multipart("executable") final String executable) throws IOException {
		checkPath(path);

		// Write the content
		log.info("Upload file {}, executable:{}", path, executable);
		final var file = new File(path);
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
		checkPath(path);
		FileUtils.delete(new File(path));
	}
}
