package org.ligoj.bootstrap.core.resource.mapper;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.CommunicationException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mail.MailSendException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;

import org.ligoj.bootstrap.core.SpringUtils;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.BusinessException;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemDialect;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Mapper exception resource test.
 */
@Path("/throw")
public class ExceptionMapperResource {

	@Autowired
	private SystemRoleRepository repository;

	/**
	 * Manual validation exception with parameters.
	 */
	@DELETE
	@Path("jsr-303")
	public void throwJsr303() {
		final ValidationJsonException exception = new ValidationJsonException();
		exception.addError("jsr303", "NotNull");
		throw exception;
	}

	/**
	 * JPA validation exception with parameters.
	 */
	@DELETE
	@Path("jsr-303-jpa")
	public void throwJsr303FromJpa() {
		final SystemRole entity = new SystemRole();

		// Name over 200 char --> BVAL exception
		entity.setName(org.apache.commons.lang3.StringUtils.repeat("-", 210));
		repository.save(entity);
	}

	/**
	 * Transaction exception with parameters.
	 */
	@DELETE
	@Path("transaction-commit")
	public void throwTransactionSystemException() {
		throw new TransactionSystemException("message");
	}

	/**
	 * Business exception with parameters.
	 */
	@DELETE
	@Path("business")
	public void throwBusiness() {
		throw new BusinessException(BusinessException.KEY_UNKNOW_ID, new IOException(), "parameter1", "parameter2");
	}

	/**
	 * Not Implemented Exception.
	 */
	@DELETE
	@Path("not-implemented")
	public void throwNotImplemented() {
		throw new NotImplementedException("message");
	}

	/**
	 * Unrecognized property.
	 * 
	 * @param user
	 *            User with invalid property.
	 */
	@POST
	@Path("unrecognized-property")
	@Consumes(MediaType.APPLICATION_JSON)
	public void throwUnrecognizedPropertyException(final SystemUser user) {
		// Cannot be called;
	}

	/**
	 * Technical exception without parameters (but could have).
	 */
	@DELETE
	@Path("technical")
	public void throwTechnical() {
		throw new TechnicalException("message", new IOException("message"));
	}

	@DELETE
	@Path("failsafe")
	public void throwFailSafe() {
		throw new IllegalStateException("message", new NullPointerException());
	}

	@DELETE
	@Path("failsafe2")
	public void throwFailSafe2() {
		throw new IllegalStateException("message", new IOException("messageio"));
	}

	@DELETE
	@Path("failsafe3")
	public void throwFailSafeThrowable() {
		throw new AssertionError("error");
	}

	@DELETE
	@Path("jax-rs")
	public void throwWebApplication() {
		throw new WebApplicationException();
	}

	@DELETE
	@Path("json-mapping")
	public void throwJSonMapping() throws IOException {
		SpringUtils.getApplicationContext().getBean("jacksonProvider", com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);
		new ObjectMapperTrim().readValue("{\"dialDouble\":\"A\"}", SystemDialect.class);
	}

	@DELETE
	@Path("transaction-begin")
	public void throwTransaction() {
		throw new CannotCreateTransactionException("message");
	}

	@DELETE
	@Path("transaction-begin2")
	public void throwTransaction2() {
		throw new CannotCreateTransactionException("message", new IOException("messageio"));
	}

	@DELETE
	@Path("connection")
	public void throwConnection() {
		throw new CannotGetJdbcConnectionException("message", new SQLException());
	}

	@DELETE
	@Path("ldap")
	public void throwCommunicationException() {
		/*
		 * As this exception is wrapped by a runtime exception brought
		 * bySpring-LDAP (optional dependency), there is no way a create a
		 * specific Mapper.
		 */
		throw new RuntimeException(new CommunicationException("Connection refused"));
	}

	@DELETE
	@Path("mail")
	public void throwMailSendException() {
		throw new MailSendException("message", new IOException("Connection refused"));
	}

	@DELETE
	@Path("security-403")
	public void throwAccessDeniedException() {
		throw new AccessDeniedException("");
	}

	@DELETE
	@Path("security-403-rs")
	public void throwForbiddenException() {
		throw new ForbiddenException();
	}

	@DELETE
	@Path("security-401")
	public void throwAuthenticationException() {
		throw new PreAuthenticatedCredentialsNotFoundException("message");
	}

	@DELETE
	@Path("jpaObjectRetrievalFailureException")
	public void throwJpaObjectRetrievalFailureException() {
		throw new JpaObjectRetrievalFailureException(new EntityNotFoundException("key"));
	}

	@DELETE
	@Path("entityNotFoundException")
	public void throwEntityNotFoundException() {
		throw new EntityNotFoundException("key");
	}

	@DELETE
	@Path("noResultException")
	public void throwNoResultException() {
		throw new EmptyResultDataAccessException("", 1, new NoResultException("message"));
	}

	@DELETE
	@Path("integrity-foreign")
	public void throwDataIntegrityException() {
		throw new DataIntegrityViolationException("",
				new IllegalStateException("bla `assignment`, CONSTRAINT `FK3D2B86CDAF555D0B` FOREIGN KEY (`project`) bla"));
	}

	@DELETE
	@Path("integrity-unicity")
	public void throwDataIntegrityUnicityException() {
		throw new DataIntegrityViolationException("", new IllegalStateException("Duplicate entry '2003' for key 'PRIMARY'"));
	}

	@DELETE
	@Path("integrity-unknown")
	public void throwDataIntegrityUnknownException() {
		throw new DataIntegrityViolationException("", new IllegalStateException("Any SQL error"));
	}

	@DELETE
	@Path("cannotAcquireLockException")
	public void throwCannotAcquireLockException() {
		throw new CannotAcquireLockException("lock");
	}

}
