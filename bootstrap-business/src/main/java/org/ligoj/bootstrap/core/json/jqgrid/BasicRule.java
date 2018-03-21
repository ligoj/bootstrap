/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json.jqgrid;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Inner class containing field rules.
 */
@Getter
@Setter
@ToString(of = { "field", "op", "data" })
public class BasicRule implements UIRule {

	/**
	 * Filter operator.
	 */
	public enum RuleOperator {

		/**
		 * Equals.
		 */
		EQ,

		/**
		 * Contains.
		 */
		CN,

		/**
		 * Not equals.
		 */
		NE,

		/**
		 * Less than.
		 */
		LT,

		/**
		 * Less than.
		 */
		LTE,

		/**
		 * Greater than or equals.
		 */
		GT,

		/**
		 * Greater than or equals.
		 */
		GTE,

		/**
		 * Begins with.
		 */
		BW,

		/**
		 * Ends with.
		 */
		EW,

		/**
		 * Custom operator.
		 */
		CT
	}

	/**
	 * Field name.
	 */
	private String field;

	/**
	 * Rule operator.
	 */
	private RuleOperator op;

	/**
	 * Filter data.
	 */
	private String data;

}