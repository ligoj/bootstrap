/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json.jqgrid;

import org.springframework.data.domain.Sort.Direction;

/**
 * A POJO that represents a sort.
 */
public class UiSort {

	/**
	 * Direction of ordered column.
	 */
	private Direction direction;

	/**
	 * Ordered column.
	 */
	private String column;

	/**
	 * Return the {@link #direction} value.
	 * 
	 * @return the {@link #direction} value.
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * Set the {@link #direction} value.
	 * 
	 * @param direction
	 *            the {@link #direction} to set.
	 */
	public void setDirection(final Direction direction) {
		this.direction = direction;
	}

	/**
	 * Return the {@link #column} value.
	 * 
	 * @return the {@link #column} value.
	 */
	public String getColumn() {
		return column;
	}

	/**
	 * Set the {@link #column} value.
	 * 
	 * @param column
	 *            the {@link #column} to set.
	 */
	public void setColumn(final String column) {
		this.column = column;
	}

}
