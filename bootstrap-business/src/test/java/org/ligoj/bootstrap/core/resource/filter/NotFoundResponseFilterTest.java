/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.OnNullReturn404;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import tools.jackson.core.JacksonException;

import java.lang.annotation.Annotation;

import static org.mockito.Mockito.*;

/**
 * ContainerResponseFilter resource test, includes {@link NotFoundResponseFilter}
 */
class NotFoundResponseFilterTest {

	private final NotFoundResponseFilter filter = new NotFoundResponseFilter() {
		@Override
		protected Object toEntity(final Object object) {
			try {
				return new ObjectMapperTrim().writeValueAsString(object);
			} catch (final JacksonException e) {
				// Ignore this error at UI level but trace it
				throw new TechnicalException("Unable to build a JSON string from a server error", e);
			}
		}
	};

	@Test
	void filterOk() {
		final var responseContext = mock(ContainerResponseContext.class);
		when(responseContext.getStatus()).thenReturn(200);
		filter.filter(null, responseContext);
	}

	@SuppressWarnings("rawtypes")
	@Test
	void filter404SingleParameter() {
		final var requestContext = mock(ContainerRequestContext.class);
		final var responseContext = mock(ContainerResponseContext.class);
		when(responseContext.getStatus()).thenReturn(204);
		final var annotation1 = mock(Annotation.class);
		final var annotation2 = mock(Annotation.class);
		final var annotations = new Annotation[] { annotation1, annotation2 };
		when((Class) annotation2.annotationType()).thenReturn(OnNullReturn404.class);
		when(responseContext.getEntityAnnotations()).thenReturn(annotations);

		final var uriInfo = mock(UriInfo.class);
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
		parameters.putSingle("id", "2000");

		when(uriInfo.getPathParameters()).thenReturn(parameters);
		when(requestContext.getUriInfo()).thenReturn(uriInfo);
		filter.filter(requestContext, responseContext);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce()).setStatus(404);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce()).setEntity(
				"{\"code\":\"entity\",\"message\":\"2000\"}", annotations, MediaType.APPLICATION_JSON_TYPE);
	}

	@Test
	void filterNoAnnotation() {
		final var requestContext = mock(ContainerRequestContext.class);
		final var responseContext = mock(ContainerResponseContext.class);
		when(responseContext.getStatus()).thenReturn(204);
		final var annotations = new Annotation[] {};
		when(responseContext.getEntityAnnotations()).thenReturn(annotations);
		filter.filter(requestContext, responseContext);
	}

	@SuppressWarnings("rawtypes")
	@Test
	void filter404NoParameter() {
		final var requestContext = mock(ContainerRequestContext.class);
		final var responseContext = mock(ContainerResponseContext.class);
		when(responseContext.getStatus()).thenReturn(204);
		final var annotation1 = mock(Annotation.class);
		final var annotation2 = mock(Annotation.class);
		final var annotations = new Annotation[] { annotation1, annotation2 };
		when((Class) annotation2.annotationType()).thenReturn(OnNullReturn404.class);
		when(responseContext.getEntityAnnotations()).thenReturn(annotations);

		final var uriInfo = mock(UriInfo.class);
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();

		when(uriInfo.getPathParameters()).thenReturn(parameters);
		when(requestContext.getUriInfo()).thenReturn(uriInfo);
		filter.filter(requestContext, responseContext);
		verify(responseContext, VerificationModeFactory.atLeastOnce()).setStatus(404);
		verify(responseContext, VerificationModeFactory.atLeastOnce())
				.setEntity("{\"code\":\"data\"}", annotations, MediaType.APPLICATION_JSON_TYPE);
	}

}