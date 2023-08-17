/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.NamedBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
    void serialize() throws JsonProcessingException {
		final var bean = new BeanA();
		bean.setName("john");
		Assertions.assertEquals("{\"entity\":\"john\"}", new ObjectMapper().writeValueAsString(new Bean(bean)));
	}

}
