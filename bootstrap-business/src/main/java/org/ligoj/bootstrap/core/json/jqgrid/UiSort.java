/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json.jqgrid;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort.Direction;

/**
 * A POJO that represents a sort.
 */
@Getter
@Setter
public class UiSort {

	/**
	 * Direction of ordered column.
	 */
	private Direction direction;

	/**
	 * Ordered column.
	 */
	private String column;

}
