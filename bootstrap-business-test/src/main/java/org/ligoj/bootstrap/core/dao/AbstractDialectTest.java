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

import java.lang.reflect.Type;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class of Dialect customizer test classes.
 */
abstract class AbstractDialectTest {
	protected BasicTypeRegistry basicTypeRegistry;

	@SuppressWarnings("unchecked")
	protected FunctionContributions newFunctionContributions() {
		final var functionContributions = mock(FunctionContributions.class);
		final var functionRegistry = new SqmFunctionRegistry();
		final var typeConfiguration = mock(TypeConfiguration.class);
		basicTypeRegistry = mock(BasicTypeRegistry.class);
		final var jdbcTypeIndicators = mock(JdbcTypeIndicators.class);

		doReturn(typeConfiguration).when(functionContributions).getTypeConfiguration();
		doReturn(basicTypeRegistry).when(typeConfiguration).getBasicTypeRegistry();
		doReturn(jdbcTypeIndicators).when(typeConfiguration).getCurrentBaseSqlTypeIndicators();
		doReturn(functionRegistry).when(functionContributions).getFunctionRegistry();
		final var basicType = mock(BasicType.class) ;
		doReturn(basicType).when(typeConfiguration).standardBasicTypeForJavaType( ArgumentMatchers.any(Type.class));
		doReturn(basicType).when(typeConfiguration).standardBasicTypeForJavaType( ArgumentMatchers.any(Class.class));
		doReturn(basicType).when(basicTypeRegistry).resolve(ArgumentMatchers.any(BasicTypeReference.class));
		doReturn(basicType).when(basicTypeRegistry).resolve(ArgumentMatchers.any(Class.class), ArgumentMatchers.any(Integer.class ));
		doReturn(basicType).when(basicTypeRegistry).getRegisteredType(ArgumentMatchers.any(Class.class ));
		return functionContributions;
	}
}
