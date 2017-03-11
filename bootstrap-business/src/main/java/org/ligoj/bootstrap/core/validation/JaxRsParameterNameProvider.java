package org.ligoj.bootstrap.core.validation;

import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.DefaultParanamer;

/**
 * JAX-RS name provider exploiting JAX-RS annotations to give the parameter name. Useful for end-user feedback and
 * validation.
 */
public class JaxRsParameterNameProvider extends ParanamerParameterNameProvider {

	/**
	 * Default constructor using a custom annotation paranmer as first-priority provider.
	 */
	public JaxRsParameterNameProvider() {
		super(new CachingParanamer(new AdaptiveParanamer(new JaxRsAnnotationParanamer(), new DefaultParanamer(), new BytecodeReadingParanamer())));
	}

}