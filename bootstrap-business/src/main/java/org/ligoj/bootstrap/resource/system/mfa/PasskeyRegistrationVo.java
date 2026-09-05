package org.ligoj.bootstrap.resource.system.mfa;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Passkey registration: the credential created by the browser (<code>navigator.credentials.create</code>), fields
 * Base64url encoded.
 */
@Getter
@Setter
public class PasskeyRegistrationVo {

	/**
	 * Device name, unique for the user.
	 */
	@NotBlank
	private String name;

	/**
	 * Credential identifier (<code>credential.id</code>).
	 */
	@NotBlank
	private String id;

	/**
	 * <code>response.clientDataJSON</code>.
	 */
	@NotBlank
	private String clientDataJSON;

	/**
	 * <code>response.attestationObject</code>.
	 */
	@NotBlank
	private String attestationObject;
}
