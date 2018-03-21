/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.template;

import java.util.Deque;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * A processor able to return a value.
 * 
 * @param <T>
 *            the source bean type.
 */
@AllArgsConstructor
@NoArgsConstructor
public class Processor<T> {

	protected Object data;

	/**
	 * Return the raw value.
	 * 
	 * @return the raw value.
	 */
	public Object getValue() {
		return data;
	}

	/**
	 * Return the raw value depending on a context.
	 * 
	 * @param context
	 *            the current context (root or loop item).
	 * @return the raw value.
	 */
	public Object getValue(final T context) {
		return data == null || data instanceof Processor<?> ? context : getValue();
	}

	/**
	 * Return the raw value depending on a context.
	 * 
	 * @param contextData
	 *            the current context (root or loop item).
	 * @return the raw value.
	 */
	@SuppressWarnings("unchecked")
	public Object getValue(final Deque<Object> contextData) {

		if (data instanceof Processor<?>) {
			// Wrapped content
			return getValue((T) ((Processor<T>) data).getValue(contextData));
		}

		return getValue((T) contextData.getLast());
	}
}
