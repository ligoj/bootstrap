package org.ligoj.bootstrap.resource.system.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;

/**
 * New implements extending org.springframework.cache.ehcache.EhCacheManagerFactoryBean
 */
@Slf4j
public class MergedEhCacheManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {

	private String configLocation;

	protected CacheManager cacheManager;

	/**
	 * Set the location of the EhCache config file(s). A typical value is "/WEB-INF/ehcache.xml".
	 * <p>
	 * Default is "ehcache.xml" in the root of the class path, or if not found,
	 * "ehcache-failsafe.xml" in the EhCache jar (default EhCache initialization).
	 * 
	 * @param configLocation
	 *            May be a simple resource file or a classpath resource matching multiple files.
	 * @see net.sf.ehcache.CacheManager#create(java.io.InputStream)
	 * @see net.sf.ehcache.CacheManager#CacheManager(java.io.InputStream)
	 */
	public void setConfigLocation(final String configLocation) {
		this.configLocation = configLocation;
	}

	@Override
	public void afterPropertiesSet() throws IOException {
		this.cacheManager = CacheManager.create(getInputStream());
	}

	@Override
	public CacheManager getObject() {
		return this.cacheManager;
	}

	@Override
	public Class<? extends CacheManager> getObjectType() {
		return this.cacheManager == null ? null : this.cacheManager.getClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() {
		this.cacheManager.shutdown();
	}

	/**
	 * This method merge ehcache xml configuration files to a single file as a stream. This merge drops the XML wrapper
	 * and the XML header, then insert back a
	 * 
	 * @return The merged inputStream
	 */
	private InputStream getInputStream() throws IOException {
		final List<String> lines = new ArrayList<>();
		for (Resource ehcacheXml : new PathMatchingResourcePatternResolver().getResources(configLocation)) {
			log.info("Load ehCache configuration {}", ehcacheXml.getURI().toString());
			lines.addAll(IOUtils.readLines(ehcacheXml.getInputStream(), StandardCharsets.UTF_8));
		}
		lines.removeIf(s -> s.startsWith("<?xml"));
		lines.removeIf(s -> s.startsWith("<ehcache"));
		lines.removeIf(s -> s.contains("xsi:"));
		lines.removeIf(s -> s.startsWith("</ehcache>"));
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8));

		// Open the wrapper
		bos.write(
				"<ehcache xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://ehcache.org/ehcache.xsd\" name=\"ECacheDatabase\">"
						.getBytes(StandardCharsets.UTF_8));

		// Copy the merged content
		for (final String line : lines) {
			bos.write(line.getBytes(StandardCharsets.UTF_8));
		}

		// Close the wrapper
		bos.write("</ehcache>".getBytes(StandardCharsets.UTF_8));
		return new ByteArrayInputStream(bos.toByteArray());
	}

}