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
class ToIdSerializerTest {

	@Getter
	@AllArgsConstructor
    static class Bean {
		@JsonSerialize(using = ToIdSerializer.class)
		private BeanA asIdInt;

		@JsonSerialize(using = ToIdSerializer.class)
		private BeanB asIdString;
	}

	class BeanA extends AbstractPersistable<Integer> {
		// Only a template class implementation
	}

	class BeanB extends AbstractPersistable<String> {
		// Only a template class implementation
	}

	@Test
    void serializeInt() throws JsonProcessingException {
		final var bean = new BeanA();
		bean.setId(1);
		Assertions.assertEquals("{\"asIdInt\":1,\"asIdString\":null}", new ObjectMapper().writeValueAsString(new Bean(bean, null)));
	}

	@Test
    void serializeString() throws JsonProcessingException {
		final var bean = new BeanB();
		bean.setId("key");
		Assertions.assertEquals("{\"asIdInt\":null,\"asIdString\":\"key\"}", new ObjectMapper().writeValueAsString(new Bean(null, bean)));
	}
}
