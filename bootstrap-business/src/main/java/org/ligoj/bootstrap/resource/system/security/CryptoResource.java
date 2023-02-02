/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
