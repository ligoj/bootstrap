/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

/**
 * JPA performance tests.
 */
public interface ISystemPerformanceJpaDao {

	/**
	 * Initialize the setup data.
	 * 
	 * @param nbEntries
	 *            the entries to initialize.
	 * @param lobData
	 *            optional LOB data.
	 * @return the setup result.
	 */
	BenchResult initialize(int nbEntries, byte[] lobData);

	/**
	 * Return the last available LOB file in the {@link org.ligoj.bootstrap.model.system.SystemBench} entity.
	 * 
	 * @return the last available LOB file in the {@link org.ligoj.bootstrap.model.system.SystemBench} entity.
	 */
	byte[] getLastAvailableLob();

	/**
	 * Perform a read for each data in the {@link org.ligoj.bootstrap.model.system.SystemBench} entity.
	 * 
	 * @return the read result.
	 */
	BenchResult benchRead();

	/**
	 * Perform a read all on the {@link org.ligoj.bootstrap.model.system.SystemBench} entity.
	 * 
	 * @return the read all result.
	 */
	BenchResult benchReadAll();

	/**
	 * Perform a update for each data in the {@link org.ligoj.bootstrap.model.system.SystemBench} entity.
	 * 
	 * @return the update result.
	 */
	BenchResult benchUpdate();

	/**
	 * Perform a delete for each data in the {@link org.ligoj.bootstrap.model.system.SystemBench} entity.
	 * 
	 * @return the delete result.
	 */
	BenchResult benchDelete();

}
