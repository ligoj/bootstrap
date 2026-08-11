/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.type.descriptor.java.JavaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class of {@link AbstractSpecification}
 */
class TestAbstractSpecificationTest {

	@Test
	void getExpressionTypeMatch() {
		var model = mock(EntityMappingType.class);
		var mapping = mock(EntityIdentifierMapping.class);
		var mappingType = mock(MappingType.class);
		var javaType = mock(JavaType.class);


		doReturn(mapping).when(model).getIdentifierMapping();
		doReturn(mappingType).when(mapping).getMappedType();
		doReturn("path").when(mapping).getAttributeName();
		doReturn(javaType).when(mappingType).getMappedJavaType();
		doReturn(Integer.class).when(javaType).getJavaTypeClass();
		Assertions.assertSame(Integer.class, AbstractSpecification.getExpressionType(model, "path"));

		var attributeMapping = mock(AttributeMapping.class);
		var attributeMappingType = mock(MappingType.class);
		var attributeJavaType = mock(JavaType.class);
		doReturn(1).when(model).getNumberOfAttributeMappings();
		doReturn(attributeMapping).when(model).getAttributeMapping(0);
		doReturn(attributeMappingType).when(attributeMapping).getMappedType();
		doReturn(attributeJavaType).when(attributeMappingType).getMappedJavaType();
		doReturn(String.class).when(attributeJavaType).getJavaTypeClass();
		doReturn("join").when(attributeMapping).getFetchableName();
		Assertions.assertSame(String.class, AbstractSpecification.getExpressionType(model, "join"));

		Assertions.assertNull(AbstractSpecification.getExpressionType(model, "other"));

	}
}
