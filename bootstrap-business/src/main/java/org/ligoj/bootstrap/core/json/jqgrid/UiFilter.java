/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json.jqgrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A POJO that represents a jQgrid JSON requests {@link String}<br>
 * A sample filter follows the following format:
 * 
 * <pre>
 * {"groupOp":"AND","rules":[{"field":"firstName","op":"eq","data":"John"}]}
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString(of = { "groupOp", "rules" })
public class UiFilter implements UIRule {

	/**
	 * Filter operator.
	 */
	public enum FilterOperator {
		/**
		 * AND : and if
		 */
		AND,

		/**
		 * OR : or else
		 */
		OR
	}

	/**
	 * Filter operator. This one is applied to each rule.
	 */
	private FilterOperator groupOp;

	/**
	 * Rules to apply.
	 */
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = BasicRule.class)
	@JsonSubTypes({ @Type(name = "group", value = UiFilter.class), @Type(name = "rule", value = BasicRule.class) })
	private List<UIRule> rules;

}
