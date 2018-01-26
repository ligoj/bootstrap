package org.ligoj.bootstrap.core.resource.filter;

import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

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
public class NotFoundResponseFilterTest {

	private NotFoundResponseFilter filter = new NotFoundResponseFilter() {
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
	public void filterOk() {
		final ContainerResponseContext responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(200);
		filter.filter(null, responseContext);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void filter404SingleParameter() {
		final ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		final ContainerResponseContext responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(204);
		final Annotation anno1 = Mockito.mock(Annotation.class);
		final Annotation anno2 = Mockito.mock(Annotation.class);
		final Annotation[] annotations = new Annotation[] { anno1, anno2 };
		Mockito.when((Class) anno2.annotationType()).thenReturn(OnNullReturn404.class);
		Mockito.when(responseContext.getEntityAnnotations()).thenReturn(annotations);

		final UriInfo uriInfo = Mockito.mock(UriInfo.class);
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
	public void filterNoAnnotation() {
		final ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		final ContainerResponseContext responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(204);
		final Annotation[] annotations = new Annotation[] {};
		Mockito.when(responseContext.getEntityAnnotations()).thenReturn(annotations);
		filter.filter(requestContext, responseContext);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void filter404NoParameter() {
		final ContainerRequestContext requestContext = Mockito.mock(ContainerRequestContext.class);
		final ContainerResponseContext responseContext = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(responseContext.getStatus()).thenReturn(204);
		final Annotation anno1 = Mockito.mock(Annotation.class);
		final Annotation anno2 = Mockito.mock(Annotation.class);
		final Annotation[] annotations = new Annotation[] { anno1, anno2 };
		Mockito.when((Class) anno2.annotationType()).thenReturn(OnNullReturn404.class);
		Mockito.when(responseContext.getEntityAnnotations()).thenReturn(annotations);

		final UriInfo uriInfo = Mockito.mock(UriInfo.class);
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();

		Mockito.when(uriInfo.getPathParameters()).thenReturn(parameters);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		filter.filter(requestContext, responseContext);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce()).setStatus(404);
		Mockito.verify(responseContext, VerificationModeFactory.atLeastOnce())
				.setEntity("{\"code\":\"data\",\"message\":null,\"parameters\":null,\"cause\":null}", annotations, MediaType.APPLICATION_JSON_TYPE);
	}

}