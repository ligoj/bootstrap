/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.type.StandardBasicTypes;

import static org.hibernate.query.sqm.produce.function.FunctionParameterType.NUMERIC;

/**
 * Register standard dialect functions.
 */
public interface RegistrableDialect {

	/**
	 * Register standard dialect functions.
	 *
	 * @param functionContributions Function registry.
	 */
	default void registerFunctions(FunctionContributions functionContributions) {
		functionContributions.getFunctionRegistry().namedDescriptorBuilder("ceil")
				.setExactArgumentCount(1).setParameterTypes(NUMERIC)
				.setInvariantType(functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.DOUBLE))
				.register();
	}
}
