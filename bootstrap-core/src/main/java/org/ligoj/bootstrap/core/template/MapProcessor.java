package org.ligoj.bootstrap.core.template;

import java.util.Map;

/**
 * A processor using bean reflection to retrieve the key of a static {@link Map}.
 * 
 * @param <T>
 *            the context bean type.
 */
public class MapProcessor<T> extends BeanProcessor<T> {

	protected final Map<?, ?> map;

	/**
	 * Property bean access constructor.
	 * 
	 * @param map
	 *            the {@link Map} hold final values..
	 * @param beanType
	 *            the source bean type.
	 * @param property
	 *            the property name.
	 */
	public MapProcessor(final Map<?, ?> map, final Class<T> beanType, final String property) {
		super(beanType, property);
		this.map = map;
	}

	@Override
	public Object getValue(final T context) {
		return getMapValue(super.getValue(context));
	}

	/**
	 * Return the {@link Map} value corresponding to given key.
	 */
	protected Object getMapValue(final Object key) {
		return map.get(key);
	}

}
