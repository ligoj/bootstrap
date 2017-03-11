package org.ligoj.bootstrap.core.validation;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.thoughtworks.paranamer.AnnotationParanamer;

/**
 * An extension of default annotation provider adding JAX-RS annotation support : {@link QueryParam},
 * {@link HeaderParam}, {@link FormParam}, {@link CookieParam}, {@link PathParam}
 */
public class JaxRsAnnotationParanamer extends AnnotationParanamer {

	/**
	 * Mapping : class to extractor
	 */
	private static final Map<Class<? extends Annotation>, Function<Annotation, String>> ANNOTATION_NAME_PROVIDERS = new LinkedHashMap<>();
	static {
		ANNOTATION_NAME_PROVIDERS.put(QueryParam.class, a -> ((QueryParam) a).value());
		ANNOTATION_NAME_PROVIDERS.put(HeaderParam.class, a -> ((HeaderParam) a).value());
		ANNOTATION_NAME_PROVIDERS.put(FormParam.class, a -> ((FormParam) a).value());
		ANNOTATION_NAME_PROVIDERS.put(CookieParam.class, a -> ((CookieParam) a).value());
		ANNOTATION_NAME_PROVIDERS.put(PathParam.class, a -> ((PathParam) a).value());
	}

	@Override
	protected String getNamedValue(final Annotation ann) {
		final String value = super.getNamedValue(ann);
		if (value == null && ANNOTATION_NAME_PROVIDERS.containsKey(ann.annotationType())) {
			return ANNOTATION_NAME_PROVIDERS.get(ann.annotationType()).apply(ann);
		}
		return value;
	}

	@Override
	protected boolean isNamed(final Annotation ann) {
		return super.isNamed(ann) || ANNOTATION_NAME_PROVIDERS.containsKey(ann.annotationType());
	}

}
