/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Test class of {@link ToIdSerializer}
 */
public class ToIdSerializerTest {

	@Getter
	@AllArgsConstructor
	public static class Bean {
		@JsonSerialize(using = ToIdSerializer.class)
		private BeanA asIdInt;

		@JsonSerialize(using = ToIdSerializer.class)
		private BeanB asIdString;
	}

	public class BeanA extends AbstractPersistable<Integer> {
		// Only a template class implementation
	}

	public class BeanB extends AbstractPersistable<String> {
		// Only a template class implementation
	}

	@Test
	public void serializeInt() throws JsonProcessingException {
		final BeanA bean = new BeanA();
		bean.setId(1);
		Assertions.assertEquals("{\"asIdInt\":1,\"asIdString\":null}", new ObjectMapper().writeValueAsString(new Bean(bean, null)));
	}

	@Test
	public void serializeString() throws JsonProcessingException {
		final BeanB bean = new BeanB();
		bean.setId("key");
		Assertions.assertEquals("{\"asIdInt\":null,\"asIdString\":\"key\"}", new ObjectMapper().writeValueAsString(new Bean(null, bean)));
	}
}
