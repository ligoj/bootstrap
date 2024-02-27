/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;

import java.util.List;

/**
 * Event based action.
 * When a successful request or event matches to <code>match</code> criteria, then the command is executed. This command received as single argument a base64 encoded JSON object payload having this structure:
 * <ul>
 *     <li><code>url</code> The original full URL</li>
 *     <li><code>query-</code> The query parameter</li>
 *     <li><code>user-</code> User triggering this request</li>
 *     <li><code>date-</code> Date this event occurred</li>
 *     <li><code>data-</code> Body of this request</li>
 * </ul>
 */
@Getter
@Setter
@Entity
@Table(name = "S_HOOK")
@JsonIgnoreProperties
public class SystemHook extends AbstractNamedAuditedEntity<Integer> {

	/**
	 * Current directory for execution
	 */
	@Length(max = 255)
	@Pattern(regexp = "\\S*")
	@NotBlank
	private String workingDirectory;

	/**
	 * Command to execute.
	 */
	@Length(max = 255)
	@NotBlank
	private String command;

	/**
	 * JSON string representing the match
	 */
	@Column(length = 1024)
	@NotBlank
	private String match;

	/**
	 * Optional list of injected configuration values from the name. Secured data is decrypted at the invocation time.
	 */
	@Convert(converter = StringListConverter.class)
	@Column(length = 1024)
	private List<String> inject;

	/**
	 * Maximum integration delay. Default value is managed by `LIGOJ_HOOK_TIMEOUT` configuration.
	 */
	@Positive
	private Integer timeout;

	/**
	 * Converted JSON object of <code>match</code> JSON string.
	 */
	@Transient
	private transient HookMatch matchObject;


}
