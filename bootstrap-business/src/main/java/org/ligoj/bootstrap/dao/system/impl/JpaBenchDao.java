/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system.impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.ligoj.bootstrap.dao.system.BenchResult;
import org.ligoj.bootstrap.dao.system.ISystemPerformanceJpaDao;
import org.ligoj.bootstrap.model.system.SystemBench;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of performance tests. This is a special DAO managing its own transaction.
 */
@Repository
@Transactional(TxType.REQUIRES_NEW)
public class JpaBenchDao implements ISystemPerformanceJpaDao {

	@PersistenceContext(unitName = "pu")
	private EntityManager em;

	@Override
	public BenchResult initialize(final int nbEntries, final byte[] lobData) {
		em.createQuery("DELETE FROM " + SystemBench.class.getName()).executeUpdate();
		for (var i = nbEntries; i-- > 0;) {
			final var perf = new SystemBench();
			perf.setPrfBool(i % 2 == 0);
			perf.setPrfChar("Performance JPA2 " + i);
			perf.setPicture(lobData);
			em.persist(perf);

			// Free memory
			em.flush();
			em.clear();
		}
		final var result = new BenchResult();
		result.setEntries(nbEntries);
		return result;
	}

	@Override
	public byte[] getLastAvailableLob() {
		final var resultList = em
				.createQuery("FROM " + SystemBench.class.getName() + " WHERE picture IS NOT NULL ORDER BY id DESC", SystemBench.class).setMaxResults(1)
				.getResultList();
		if (resultList.isEmpty()) {
			return new byte[0];
		}
		return resultList.getFirst().getPicture();
	}

	@Override
	public BenchResult benchRead() {
		final var entries = getEntries();
		for (final int prfId : entries) {
			em.find(SystemBench.class, prfId);

			// Free memory
			em.clear();
		}
		final var benchResult = new BenchResult();
		benchResult.setEntries(entries.size());
		return benchResult;
	}

	@Override
	public BenchResult benchUpdate() {
		final var entries = getEntries();
		for (final int prfId : entries) {
			em.createQuery("UPDATE " + SystemBench.class.getName() + " SET prfBool = false WHERE id = :prfId").setParameter("prfId", prfId)
					.executeUpdate();
		}
		final var benchResult = new BenchResult();
		benchResult.setEntries(entries.size());
		return benchResult;
	}

	/**
	 * Return entries identifiers.
	 */
	private List<Integer> getEntries() {
		return em.createQuery("SELECT id FROM " + SystemBench.class.getName(), Integer.class).getResultList();
	}

	@Override
	public BenchResult benchDelete() {
		final var entries = getEntries();
		for (final int prfId : entries) {
			em.createQuery("DELETE " + SystemBench.class.getName() + " WHERE id = :prfId").setParameter("prfId", prfId).executeUpdate();
		}
		final var benchResult = new BenchResult();
		benchResult.setEntries(entries.size());
		return benchResult;
	}

	@Override
	public BenchResult benchReadAll() {
		final var entries = em.createQuery("FROM " + SystemBench.class.getName(), SystemBench.class).getResultList();
		final var benchResult = new BenchResult();
		benchResult.setEntries(entries.size());
		return benchResult;
	}

}
