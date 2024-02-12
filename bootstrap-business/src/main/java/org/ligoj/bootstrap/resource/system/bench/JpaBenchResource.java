/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.bench;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.ligoj.bootstrap.core.resource.OnNullReturn404;
import org.ligoj.bootstrap.dao.system.BenchResult;
import org.ligoj.bootstrap.dao.system.ISystemPerformanceJpaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Mixed JDBC and JPA transaction (or not) benchmarks. Note that there is no transaction management in this resource
 * since the transaction time has to be computed.
 */
@Path("/system/bench")
@Produces(MediaType.APPLICATION_JSON)
@Service
@Slf4j
public class JpaBenchResource {

	/**
	 * JPA DAO provider for performance.
	 */
	@Autowired
	private ISystemPerformanceJpaDao jpaDao;

	/**
	 * Initialize data for next bench tests.
	 *
	 * @param blob the BLOB file to attach.
	 * @param nb   the amount of data to persist.
	 * @return The bench result. The return type is text/html for IE7 support.
	 * @throws IOException When the blob cannot be read.
	 */
	@POST
	@PUT
	@Produces(MediaType.TEXT_HTML)
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
	@Path("prepare")
	public String prepareData(@Multipart(value = "nb", required = false) final String nb,
			@Multipart(value = "blob", required = false) final InputStream blob
	) throws IOException {
		final var start = System.currentTimeMillis();
		final var lobData = blob == null ? new byte[0] : IOUtils.toByteArray(blob);
		log.info("Content size :" + lobData.length);
		final var result = jpaDao.initialize(Integer.parseInt(nb), lobData);
		result.setDuration(System.currentTimeMillis() - start);
		return new org.ligoj.bootstrap.core.json.ObjectMapperTrim().writeValueAsString(result);
	}

	/**
	 * Bench the read operations.
	 *
	 * @return The bench result data.
	 */
	@GET
	@Path("read")
	public BenchResult benchRead() {
		final var start = System.currentTimeMillis();
		final var result = jpaDao.benchRead();
		result.setDuration(System.currentTimeMillis() - start);
		return result;
	}

	/**
	 * Bench the read operations.
	 *
	 * @return The bench result data.
	 */
	@GET
	@Path("read/all")
	public BenchResult benchReadAll() {
		final var start = System.currentTimeMillis();
		final var result = jpaDao.benchReadAll();
		result.setDuration(System.currentTimeMillis() - start);
		return result;
	}

	/**
	 * Bench the update operations.
	 *
	 * @return The bench result data.
	 */
	@PUT
	@Path("update")
	public BenchResult benchUpdate() {
		final var start = System.currentTimeMillis();
		final var result = jpaDao.benchUpdate();
		result.setDuration(System.currentTimeMillis() - start);
		return result;
	}

	/**
	 * Bench the delete operations.
	 *
	 * @return The bench result data.
	 */
	@DELETE
	@Path("delete")
	public BenchResult benchDelete() {
		final var start = System.currentTimeMillis();
		final var result = jpaDao.benchDelete();
		result.setDuration(System.currentTimeMillis() - start);
		return result;
	}

	/**
	 * Bench the get picture file.
	 *
	 * @return the JAX-RS stream.
	 */
	@GET
	@Path("picture.png")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@OnNullReturn404
	public StreamingOutput downloadLobFile() {
		log.info("Picture download is requested");
		final var firstAvailableLob = jpaDao.getLastAvailableLob();
		if (firstAvailableLob.length == 0) {
			return null;
		}
		return output -> {
			try {
				IOUtils.write(firstAvailableLob, output);
			} catch (final IOException e) {
				throw new IllegalStateException("Unable to write the LOB data", e);
			}
		};
	}

}
