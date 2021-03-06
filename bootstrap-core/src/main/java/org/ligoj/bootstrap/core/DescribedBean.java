/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.validation.SafeHtml;

import lombok.Getter;
import lombok.Setter;

/**
 * A described bean
 *
 * @param <K>
 *            The type of the identifier
 */
@Getter
@Setter
public class DescribedBean<K extends Serializable> extends NamedBean<K> implements IDescribableBean<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@Length(max = 250)
	@SafeHtml
	private String description;

	/**
	 * From a {@link IDescribableBean} to another {@link IDescribableBean} bean copy.
	 *
	 * @param <T>
	 *            Bean type.
	 * @param from
	 *            the {@link IDescribableBean} source. Must not be null.
	 * @param to
	 *            the target {@link IDescribableBean} to fill. Must not be null.
	 */
	public static <T extends Serializable> void copy(final IDescribableBean<T> from, final IDescribableBean<T> to) {
		NamedBean.copy(from, to);
		to.setDescription(from.getDescription());
	}

	/**
	 * From a {@link IDescribableBean} to a new {@link DescribedBean} bean clone.
	 *
	 * @param <T>
	 *            Bean type.
	 * @param from
	 *            the {@link IDescribableBean} source. May be null.
	 * @return <code>null</code> or new {@link DescribedBean} instance copied from the source.
	 */
	public static <T extends Serializable> DescribedBean<T> clone(final IDescribableBean<T> from) {
		if (from == null) {
			return null;
		}
		final var bean = new DescribedBean<T>();
		copy(from, bean);
		return bean;
	}

}
