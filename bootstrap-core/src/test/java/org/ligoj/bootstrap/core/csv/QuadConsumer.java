/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts four input arguments and returns no result. This is the four-arity
 * specialization of {@link Consumer}. Unlike most other functional interfaces, {@code BiConsumer} is expected to
 * operate via side-effects.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Object, Object)}.
 *
 * @param <T>
 *            the type of the first argument to the operation
 * @param <U>
 *            the type of the second argument to the operation
 *
 * @see Consumer
 */
@FunctionalInterface
public interface QuadConsumer<T, P, Q, R> {

	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param t
	 *            the first input argument
	 * @param p
	 *            the second input argument
	 * @param q
	 *            the third input argument
	 * @param r
	 *            the fourth input argument
	 */
	void accept(T t, P p, Q q, R r);
}
