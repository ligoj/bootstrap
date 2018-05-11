/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.bench;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.ligoj.bootstrap.core.resource.OnNullReturn404;
import org.ligoj.bootstrap.dao.system.BenchResult;
import org.ligoj.bootstrap.dao.system.ISystemPerformanceJpaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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
	 * @param blob
	 *            the BLOB file to attach.
	 * @param nb
	 *            the amount of data to persist.
	 * @return the the bench result. The return type is text/html for IE7 support.
	 * @throws IOException
	 *             When the blob cannot be read.
	 */
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
	public String prepareData(@Multipart(value = "blob", required = false) final InputStream blob,
			@Multipart("nb") final int nb) throws IOException {
		final long start = System.currentTimeMillis();
		final byte[] lobData = blob == null ? new byte[0] : IOUtils.toByteArray(blob);
		log.info("Content size :" + lobData.length);
		final BenchResult result = jpaDao.initialize(nb, lobData);
		result.setDuration(System.currentTimeMillis() - start);
		return new org.ligoj.bootstrap.core.json.ObjectMapperTrim().writeValueAsString(result);
	}

	/**
	 * Bench the read operations.
	 *
	 * @return The bench result data.
	 */
	@GET
	public BenchResult benchRead() {
		final long start = System.currentTimeMillis();
		final BenchResult result = jpaDao.benchRead();
		result.setDuration(System.currentTimeMillis() - start);
		return result;
	}

	/**
	 * Bench the read operations.
	 *
	 * @return The bench result data.
	 */
	@GET
	@Path("all")
	public BenchResult benchReadAll() {
		final long start = System.currentTimeMillis();
		final BenchResult result = jpaDao.benchReadAll();
		result.setDuration(System.currentTimeMillis() - start);
		return result;
	}

	/**
	 * Bench the update operations.
	 *
	 * @return The bench result data.
	 */
	@PUT
	public BenchResult benchUpdate() {
		final long start = System.currentTimeMillis();
		final BenchResult result = jpaDao.benchUpdate();
		result.setDuration(System.currentTimeMillis() - start);
		return result;
	}

	/**
	 * Bench the delete operations.
	 *
	 * @return The bench result data.
	 */
	@DELETE
	public BenchResult benchDelete() {
		final long start = System.currentTimeMillis();
		final BenchResult result = jpaDao.benchDelete();
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
		final byte[] firstAvailableLob = jpaDao.getLastAvailableLob();
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
