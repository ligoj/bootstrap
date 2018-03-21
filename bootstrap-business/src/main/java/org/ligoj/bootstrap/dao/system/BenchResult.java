/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

/**
 * Bench result.
 */
public class BenchResult {

	/**
	 * Bench duration.
	 */
	private long duration;

	/**
	 * Managed entries.
	 */
	private int entries;

	/**
	 * Return the {@link #duration} value.
	 * 
	 * @return the {@link #duration} value.
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Set the {@link #duration} value.
	 * 
	 * @param duration
	 *            the {@link #duration} to set.
	 */
	public void setDuration(final long duration) {
		this.duration = duration;
	}

	/**
	 * Return the {@link #entries} value.
	 * 
	 * @return the {@link #entries} value.
	 */
	public int getEntries() {
		return entries;
	}

	/**
	 * Set the {@link #entries} value.
	 * 
	 * @param entries
	 *            the {@link #entries} to set.
	 */
	public void setEntries(final int entries) {
		this.entries = entries;
	}
}
