/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.Map;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * JPA association fetch helper
 */
@Component
public class FetchHelper {

	/**
	 * Apply the fetch criteria.
	 * 
	 * @param <T>
	 *            JPA entity type to fetch.
	 * @param fetchedAssociations
	 *            A map of association to fetch. The map keys for composites associations should not have two times the
	 *            same identifier &lt;"contract.contract", JoinType.INNER&gt; is not possible although
	 *            &lt;"contracts.contract", JoinType.INNER&gt; is accepted.
	 * @param root
	 *            Criteria {@link Root}.
	 */
	public <T> void applyFetchedAssociations(final Map<String, JoinType> fetchedAssociations, final Root<T> root) {
		for (final var entry : fetchedAssociations.entrySet()) {
			final var joinType = entry.getValue();
			fetchAssociation(root, entry, joinType);
		}
	}

	/**
	 * Fetch association.
	 */
	private <T> void fetchAssociation(final Root<T> root, final Map.Entry<String, JoinType> entry, final JoinType joinType) {
		final var propertiesToFetch = StringUtils.split(entry.getKey(), '.');
		FetchParent<?, ?> previouslyFetched = root;
		for (final var property : propertiesToFetch) {
			previouslyFetched = getFetchedAssoc(previouslyFetched, joinType, property);
		}
	}

	private Fetch<?, ?> getFetchedAssoc(final FetchParent<?, ?> parent, final JoinType joinType, final String propertyToFetch) {

		// Search within current fetches
		for (final Fetch<?, ?> fetch : parent.getFetches()) {
			if (fetch.getAttribute().getName().equals(propertyToFetch)) {
				return fetch;
			}
		}

		// Create a new one
		return parent.fetch(propertyToFetch, joinType);
	}

}
