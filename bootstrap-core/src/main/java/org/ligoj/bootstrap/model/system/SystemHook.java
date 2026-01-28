/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;

import java.util.List;

/**
 * Event based action. When a successful request or event matches to <code>match</code> criteria, then the command is executed. This command
 * received as single argument a base64 encoded JSON object payload having this structure:
 * <ul>
 * <li><code>path</code> The original REST path of called API</li>
 * <li><code>name</code> The hook name</li>
 * <li><code>user</code> Principal triggering this request</li>
 * <li><code>now</code> Date this event occurred</li>
 * <li><code>method</code> Related REST API method</li>
 * <li><code>api</code> Related REST API name</li>
 * <li><code>inject</code> Injected secret and configuration values</li>
 * <li><code>result</code> Body of this request</li>
 * <li><code>timeout</code> Timeout value in seconds, staring from the `now`</li>
 * </ul>
 * <p>
 * When "delay" is 0, the hook is executed synchronously. Otherwise, it is executed asynchronously. Synchronous executions gets an
 * additional custom boolean header "x-ligoj-hook-NAME" depending on the success or not.
 */
@Getter
@Setter
@Entity
@Table(name = "S_HOOK", uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
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
	 * Maximum integration delay (in seconds). Default value is managed by `LIGOJ_HOOK_TIMEOUT` configuration. Default is `10` seconds.
	 */
	@Positive
	private Integer timeout;

	/**
	 * Minimum delay (in seconds) before asynchronous execution of this hook. Default value is 1. When 0, execution is synchronous.
	 */
	@PositiveOrZero
	private Integer delay;

}
