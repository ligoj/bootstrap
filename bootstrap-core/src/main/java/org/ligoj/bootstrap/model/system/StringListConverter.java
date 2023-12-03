/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple string to list converter based on ',' char separator.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
	private static final String SPLIT_CHAR = ";";

	@Override
	public String convertToDatabaseColumn(final List<String> stringList) {
		return CollectionUtils.isEmpty(stringList) ? null : String.join(SPLIT_CHAR, stringList);
	}

	@Override
	public List<String> convertToEntityAttribute(final String string) {
		return StringUtils.isEmpty(string) ? Collections.emptyList() : Arrays.asList(string.split(SPLIT_CHAR));
	}
}