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
import org.mockito.Mockito;

/**
 * Test class of {@link AbstractSpecification}
 */
class TestAbstractSpecificationTest {

	@Test
	void getExpressionTypeMatch() {
		var model = Mockito.mock(EntityMappingType.class);
		var mapping = Mockito.mock(EntityIdentifierMapping.class);
		var mappingType = org.mockito.Mockito.mock(MappingType.class);
		var javaType = Mockito.mock(JavaType.class);


		Mockito.doReturn(mapping).when(model).getIdentifierMapping();
		Mockito.doReturn(mappingType).when(mapping).getMappedType();
		Mockito.doReturn("path").when(mapping).getAttributeName();
		Mockito.doReturn(javaType).when(mappingType).getMappedJavaType();
		Mockito.doReturn(Integer.class).when(javaType).getJavaTypeClass();
		Assertions.assertSame(Integer.class, AbstractSpecification.getExpressionType(model, "path"));

		var attributeMapping = Mockito.mock(AttributeMapping.class);
		var attributeMappingType = org.mockito.Mockito.mock(MappingType.class);
		var attributeJavaType = Mockito.mock(JavaType.class);
		Mockito.doReturn(1).when(model).getNumberOfAttributeMappings();
		Mockito.doReturn(attributeMapping).when(model).getAttributeMapping(0);
		Mockito.doReturn(attributeMappingType).when(attributeMapping).getMappedType();
		Mockito.doReturn(attributeJavaType).when(attributeMappingType).getMappedJavaType();
		Mockito.doReturn(String.class).when(attributeJavaType).getJavaTypeClass();
		Mockito.doReturn("join").when(attributeMapping).getFetchableName();
		Assertions.assertSame(String.class, AbstractSpecification.getExpressionType(model, "join"));

		Assertions.assertNull(AbstractSpecification.getExpressionType(model, "other"));

	}
}
