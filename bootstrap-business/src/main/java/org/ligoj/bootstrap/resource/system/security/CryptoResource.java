package org.ligoj.bootstrap.resource.system.security;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.ligoj.bootstrap.core.crypto.CryptoHelper;

/**
 * Cryptographic management.
 */
@Path("/system/security/crypto")
@Service
@Transactional
public class CryptoResource {

	@Autowired
	private CryptoHelper cryptoHelper;

	/**
	 * Encrypt a value
	 * 
	 * @param value
	 *            The value to encrypt.
	 * @return The encrypted value.
	 */
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String create(final String value) {
		return cryptoHelper.encrypt(value);
	}
}
