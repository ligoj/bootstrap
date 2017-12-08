package org.ligoj.bootstrap.core.model;

import org.junit.Assert;
import org.junit.Test;
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
public class ToNameSerializerTest {

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Bean {
		@JsonSerialize(using = ToNameSerializer.class)
		private BeanA entity;
	}

	public class BeanA extends NamedBean<Integer> {

	}

	@Test
	public void serialize() throws JsonProcessingException {
		final BeanA bean = new BeanA();
		bean.setName("john");
		Assert.assertEquals("{\"entity\":\"john\"}", new ObjectMapper().writeValueAsString(new Bean(bean)));
	}

}
