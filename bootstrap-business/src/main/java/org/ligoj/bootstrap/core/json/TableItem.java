package org.ligoj.bootstrap.core.json;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple wrapper item for test.
 * 
 * @param <K>
 *            Wrapped data type
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class TableItem<K> {
	/**
	 * Records in data base.
	 */
	private long recordsTotal;

	/**
	 * Records in data base when filtering has been applied. Useless while filtering is not enabled; since is equal to
	 * {@link #recordsTotal}.
	 */
	private long recordsFiltered;

	/**
	 * Draw
	 */
	private String draw;

	/**
	 * data
	 */
	private List<K> data;

	/**
	 * Return the Records in data base.
	 * 
	 * @return the Records in data base.
	 */
	public long getRecordsTotal() {
		return recordsTotal;
	}

	/**
	 * Return the records in data base when filtering has been applied. Useless while filtering is not enabled; since is
	 * equal to {@link #recordsTotal}.
	 * 
	 * @return Records in data base when filtering has been applied. Useless while filtering is not enabled; since is
	 *         equal to {@link #recordsTotal}.
	 */
	public long getRecordsFiltered() {
		return recordsFiltered;
	}

	/**
	 * @return the Draw
	 */
	public String getDraw() {
		return draw;
	}

	/**
	 * Return the {@link #data} value.
	 * 
	 * @return the {@link #data} value.
	 */
	public List<K> getData() {
		return data;
	}

	/**
	 * Set the {@link #data} value.
	 * 
	 * @param data
	 *            the {@link #data} to set.
	 */
	public void setData(final List<K> data) {
		this.data = data;
	}

	/**
	 * Set the {@link #recordsTotal} value.
	 * 
	 * @param recordsTotal
	 *            the {@link #recordsTotal} to set.
	 */
	public void setRecordsTotal(final long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	/**
	 * Set the {@link #recordsFiltered} value.
	 * 
	 * @param recordsFiltered
	 *            the {@link #recordsFiltered} to set.
	 */
	public void setRecordsFiltered(final long recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	/**
	 * Set the {@link #draw} value.
	 * 
	 * @param draw
	 *            the {@link #draw} to set.
	 */
	public void setDraw(final String draw) {
		this.draw = draw;
	}
}
