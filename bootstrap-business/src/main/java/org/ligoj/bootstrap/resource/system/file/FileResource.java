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
import org.springframework.stereotype.Service;

import java.io.*;

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
	 * Download a remote file.
	 *
	 * @param path Source file path.
	 * @return Source content stream.
	 */
	@GET
	public InputStream download(@QueryParam("path") String path) throws IOException {
		return new FileInputStream(path);
	}

	/**
	 * Create a hook with file content.
	 *
	 * @param content    Target file content.
	 * @param path       Target file path.
	 * @param executable Make the file executable when 'true'.
	 */
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void upload(@Multipart(value = "content") final InputStream content, @Multipart(value = "path") final String path, @Multipart(value = "executable") final String executable) throws IOException {
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
	 */
	@DELETE
	public void delete(@QueryParam("path") final String path) throws IOException {
		FileUtils.delete(new File(path));
	}
}
