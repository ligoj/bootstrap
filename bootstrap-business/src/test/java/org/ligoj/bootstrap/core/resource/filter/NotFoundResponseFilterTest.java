/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import java.lang.annotation.Annotation;

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

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * ContainerResponseFilter resource test, includes {@link NotFoundResponseFilter}
 */
class NotFoundResponseFilterTest {

	private final NotFoundResponseFilter filter = new NotFoundResponseFilter() {
		@Override
		protected Object toEntity(final Object object) {
			try {
				return new ObjectMapperTrim().writeValueAsString(object);
			} catch (final JsonProcessingException e) {
				// Ignore this error at UI level but trace it
				throw new TechnicalException("Unable to build a JSON string from a server error", e);
			}
		}
	};

	@Test
	void filterOk() {
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(200);
		filter.filter(null, responseContext);
	}

	@SuppressWarnings("rawtypes")
	@Test
	void filter404SingleParameter() {
		final var requestContext = Mockito.mock(ContainerRequestContext.class);
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(204);
		final var annotation1 = Mockito.mock(Annotation.class);
		final var annotation2 = Mockito.mock(Annotation.class);
		final var annotations = new Annotation[] { annotation1, annotation2 };
		Mockito.when((Class) annotation2.annotationType()).thenReturn(OnNullReturn404.class);
		Mockito.when(responseContext.getEntityAnnotations()).thenReturn(annotations);

		final var uriInfo = Mockito.mock(UriInfo.class);
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
		parameters.putSingle("id", "2000");

		Mockito.when(uriInfo.getPathParameters()).thenReturn(parameters);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		filter.filter(requestContext, responseContext);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce()).setStatus(404);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce()).setEntity(
				"{\"code\":\"entity\",\"message\":\"2000\",\"parameters\":null,\"cause\":null}", annotations, MediaType.APPLICATION_JSON_TYPE);
	}

	@Test
	void filterNoAnnotation() {
		final var requestContext = Mockito.mock(ContainerRequestContext.class);
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(204);
		final var annotations = new Annotation[] {};
		Mockito.when(responseContext.getEntityAnnotations()).thenReturn(annotations);
		filter.filter(requestContext, responseContext);
	}

	@SuppressWarnings("rawtypes")
	@Test
	void filter404NoParameter() {
		final var requestContext = Mockito.mock(ContainerRequestContext.class);
		final var responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(204);
		final var annotation1 = Mockito.mock(Annotation.class);
		final var annotation2 = Mockito.mock(Annotation.class);
		final var annotations = new Annotation[] { annotation1, annotation2 };
		Mockito.when((Class) annotation2.annotationType()).thenReturn(OnNullReturn404.class);
		Mockito.when(responseContext.getEntityAnnotations()).thenReturn(annotations);

		final var uriInfo = Mockito.mock(UriInfo.class);
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();

		Mockito.when(uriInfo.getPathParameters()).thenReturn(parameters);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		filter.filter(requestContext, responseContext);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce()).setStatus(404);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce())
				.setEntity("{\"code\":\"data\",\"message\":null,\"parameters\":null,\"cause\":null}", annotations, MediaType.APPLICATION_JSON_TYPE);
	}

}