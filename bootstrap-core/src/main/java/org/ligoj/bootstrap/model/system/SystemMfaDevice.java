package org.ligoj.bootstrap.model.system;

import java.time.Instant;

import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A multi-factor authentication device registered by a user: a TOTP authenticator application, or a passkey
 * (WebAuthn). The secret (TOTP seed, or the passkey credential data) is stored encrypted and never serialized. A
 * user may register several devices, each named uniquely; one of them is the default one proposed at verification.
 */
@Entity
@Table(name = "S_MFA_DEVICE", uniqueConstraints = @UniqueConstraint(columnNames = { "user", "name" }))
@Getter
@Setter
@ToString(of = { "user", "name" })
public class SystemMfaDevice extends AbstractNamedAuditedEntity<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * TOTP device type (RFC 6238 authenticator applications).
	 */
	public static final String TYPE_TOTP = "TOTP";

	/**
	 * Passkey device type (WebAuthn / FIDO2: platform authenticators such as Touch ID, security keys).
	 */
	public static final String TYPE_PASSKEY = "PASSKEY";

	/**
	 * Owner login.
	 */
	@NotNull
	private String user;

	/**
	 * Device type, see {@link #TYPE_TOTP}.
	 */
	@NotNull
	private String type = TYPE_TOTP;

	/**
	 * Encrypted secret: the Base32 TOTP seed, or the passkey credential (identifier, public key, algorithm, signature
	 * counter) as JSON. Sized for a passkey record: a schema created by an earlier version keeps a 255 characters
	 * column, to be widened (<code>ALTER TABLE S_MFA_DEVICE ALTER COLUMN SECRET TYPE VARCHAR(4000)</code>).
	 */
	@NotNull
	@JsonIgnore
	@Column(length = 4000)
	private String secret;

	/**
	 * Last successful verification with this device.
	 */
	private Instant lastUsed;

	/**
	 * When <code>true</code>, this device is the default one, proposed first at verification. The first registered
	 * device is the default one; removing it hands the role to the oldest remaining device. Nullable wrapper: rows
	 * created before this column existed hold <code>null</code>, read as <code>false</code>.
	 */
	@Column(name = "is_default")
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Boolean defaultDevice;

	/**
	 * @return <code>true</code> when this device is the default one.
	 */
	public boolean isDefaultDevice() {
		return Boolean.TRUE.equals(defaultDevice);
	}

	/**
	 * @param defaultDevice <code>true</code> to make this device the default one.
	 */
	public void setDefaultDevice(final boolean defaultDevice) {
		this.defaultDevice = defaultDevice;
	}
}
