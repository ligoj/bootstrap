/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.type.StandardBasicTypes;

import static org.hibernate.query.sqm.produce.function.FunctionParameterType.NUMERIC;

/**
 * HSQL dialect with keyword registration.
 */
public class HSQLDialect25 extends HSQLDialect {

	/**
	 * Default dialect constructor adding the missing features.
	 */
	public HSQLDialect25() {
		super();
		registerKeyword("PERIOD");
	}


	@Override
	public void initializeFunctionRegistry(FunctionContributions functionContributions) {
		super.initializeFunctionRegistry(functionContributions);
		registerFunctions(functionContributions);
	}

	private void registerFunctions(FunctionContributions functionContributions) {
		functionContributions.getFunctionRegistry().namedDescriptorBuilder("ceil")
				.setExactArgumentCount(1).setParameterTypes(NUMERIC)
				.setInvariantType(functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.DOUBLE))
				.register();
	}

}
