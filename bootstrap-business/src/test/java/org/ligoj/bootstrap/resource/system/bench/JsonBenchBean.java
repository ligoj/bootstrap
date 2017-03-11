package org.ligoj.bootstrap.resource.system.bench;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple collection wrapper in order to perform a performance test with Jackson and Jettison providers.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class JsonBenchBean {

	/**
	 * Bean collection (some items for performance test)
	 */
	@XmlElement
	private List<SimpleJsonBean> beans;

	/**
	 * Return the {@link #beans} value.
	 * 
	 * @return the {@link #beans} value.
	 */
	public List<SimpleJsonBean> getBeans() {
		return beans;
	}

	/**
	 * Set the {@link #beans} value.
	 * 
	 * @param beans
	 *            the {@link #beans} to set.
	 */
	public void setBeans(final List<SimpleJsonBean> beans) {
		this.beans = beans;
	}

}
