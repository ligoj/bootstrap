package org.ligoj.bootstrap.resource.system.mfa;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * A code to verify.
 */
@Getter
@Setter
public class MfaCodeVo {

	/**
	 * The code typed by the user: the 6 digits of an authenticator, or an access key.
	 */
	@NotBlank
	private String code;

	/**
	 * Identifier of the device selected by the user; <code>null</code> to try every device.
	 */
	private Integer device;
}
