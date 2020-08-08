/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao.csv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Multiple classpath resource locator using CameL cases, lower case combinations.
 */
@Slf4j
public class ClassPathResourceMultiple {

	/**
	 * Multiple path.
	 */
	private final Set<String> multiplePath = new LinkedHashSet<>();

	/**
	 * Multiple path.
	 */
	private String lastPath;

	/**
	 * Initialize the classpath resources.
	 *
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param clazz
	 *            the bean type.
	 */
	public ClassPathResourceMultiple(final String csvRoot, final Class<?> clazz) {
		addCombinations(csvRoot, clazz);
		multiplePath.remove(lastPath);
	}

	/**
	 * Add combinations of resource names of given entity class.
	 */
	private void addCombinations(final String csvRoot, final Class<?> entityClass) {
		final var name = entityClass.getSimpleName();
		addCombination(csvRoot + "/" + name.toLowerCase(Locale.ENGLISH) + ".csv");
		addCombination(csvRoot + "/" + String.join("-", StringUtils.splitByCharacterTypeCamelCase(name)).toLowerCase(Locale.ENGLISH) + ".csv");
	}

	/**
	 * Add combination
	 */
	private void addCombination(final String path) {
		this.multiplePath.add(path);
		lastPath = path;
	}

	/**
	 * Return the first available input stream.
	 *
	 * @return the first available input stream.
	 * @throws IOException
	 *             No resource has been found.
	 */
	public InputStream getInputStream() throws IOException {
		for (final var path : multiplePath) {
			try {
				return new ClassPathResource(path).getInputStream();
			} catch (final FileNotFoundException e) { // NOSONAR - Ignore this error, continue
				log.info(path + " resource has not been found, try next one : {}", e.getMessage());
			}
		}
		return new ClassPathResource(lastPath).getInputStream();
	}

}
