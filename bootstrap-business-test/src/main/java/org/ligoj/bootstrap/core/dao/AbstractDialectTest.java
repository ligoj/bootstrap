/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.type.BasicType;
import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.spi.TypeConfiguration;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.lang.reflect.Type;

/**
 * Test class of Dialect customizer test classes.
 */
abstract class AbstractDialectTest {
	protected BasicTypeRegistry basicTypeRegistry;

	@SuppressWarnings("unchecked")
	protected FunctionContributions newFunctionContributions() {
		final var functionContributions = Mockito.mock(FunctionContributions.class);
		final var functionRegistry = new SqmFunctionRegistry();
		final var typeConfiguration = Mockito.mock(TypeConfiguration.class);
		basicTypeRegistry = Mockito.mock(BasicTypeRegistry.class);
		final var jdbcTypeIndicators = Mockito.mock(JdbcTypeIndicators.class);

		Mockito.doReturn(typeConfiguration).when(functionContributions).getTypeConfiguration();
		Mockito.doReturn(basicTypeRegistry).when(typeConfiguration).getBasicTypeRegistry();
		Mockito.doReturn(jdbcTypeIndicators).when(typeConfiguration).getCurrentBaseSqlTypeIndicators();
		Mockito.doReturn(functionRegistry).when(functionContributions).getFunctionRegistry();
		final var basicType =Mockito.mock(BasicType.class) ;
		Mockito.doReturn(basicType).when(typeConfiguration).standardBasicTypeForJavaType( ArgumentMatchers.any(Type.class));
		Mockito.doReturn(basicType).when(typeConfiguration).standardBasicTypeForJavaType( ArgumentMatchers.any(Class.class));
		Mockito.doReturn(basicType).when(basicTypeRegistry).resolve(ArgumentMatchers.any(BasicTypeReference.class));
		return functionContributions;
	}
}
