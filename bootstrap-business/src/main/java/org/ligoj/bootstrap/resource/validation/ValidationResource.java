package org.ligoj.bootstrap.resource.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.ligoj.bootstrap.core.validation.ValidatorBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Validation resource.
 */
@Path("/validation")
@Service
public class ValidationResource {

	@Autowired
	private ValidatorBean validator;

	/**
	 * Return the validation descriptors of given bean.
	 * 
	 * @param className
	 *            the class to describe.
	 * @return the validation descriptors of given bean.
	 * @throws ClassNotFoundException
	 *             when the bean is not found.
	 */
	@GET
	public Map<String, List<String>> describe(final String className) throws ClassNotFoundException {
		final Class<?> beanClass = Class.forName(className);
		final Map<String, List<String>> result = new HashMap<>();
		for (final PropertyDescriptor property : validator.getValidator().getConstraintsForClass(beanClass).getConstrainedProperties()) {
			final List<String> list = new ArrayList<>();
			result.put(property.getPropertyName(), list);
			for (final ConstraintDescriptor<?> constraint : property.getConstraintDescriptors()) {
				// Since constraints are annotation, get the annotation class (interface)
				list.add(constraint.getAnnotation().getClass().getInterfaces()[0].getName());
			}
		}

		return result;
	}
}
