/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonSerialize;

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

	static class BeanA extends AbstractPersistable<Integer> {
		// Only a template class implementation
	}

	static class BeanB extends AbstractPersistable<String> {
		// Only a template class implementation
	}

	@Test
    void serializeInt() throws JacksonException {
		final var bean = new BeanA();
		bean.setId(1);
		Assertions.assertEquals("{\"asIdInt\":1,\"asIdString\":null}", new ObjectMapper().writeValueAsString(new Bean(bean, null)));
	}

	@Test
    void serializeString() throws JacksonException {
		final var bean = new BeanB();
		bean.setId("key");
		Assertions.assertEquals("{\"asIdInt\":null,\"asIdString\":\"key\"}", new ObjectMapper().writeValueAsString(new Bean(null, bean)));
	}
}
