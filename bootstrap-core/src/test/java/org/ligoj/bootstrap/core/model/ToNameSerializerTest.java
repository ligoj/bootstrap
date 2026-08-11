/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.NamedBean;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Test class of {@link ToNameSerializer}
 */
class ToNameSerializerTest {

	@Getter
	@Setter
	@AllArgsConstructor
    static class Bean {
		@JsonSerialize(using = ToNameSerializer.class)
		private BeanA entity;
	}

	static class BeanA extends NamedBean<Integer> {

		/**
		 * SID
		 */
		private static final long serialVersionUID = 1L;
		// Nothing to add
	}

	@Test
    void serialize() throws JacksonException {
		final var bean = new BeanA();
		bean.setName("john");
		Assertions.assertEquals("{\"entity\":\"john\"}", new ObjectMapper().writeValueAsString(new Bean(bean)));
	}

}
