/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;

import java.lang.annotation.*;

/**
 * Annotation used as flag for 404 HTTP status management on null return of JAX-RS method.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnNullReturn404 {

	// Simple marker for JAX-RS method.
}
