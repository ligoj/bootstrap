/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.model.AbstractNamedEntity;

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
public class SystemHook extends AbstractNamedEntity<Integer> {

	/**
	 * Current directory for execution
	 */
	@Length(max = 255)
	private String workingDirectory;

	/**
	 * Command to execute.
	 */
	@Length(max = 255)
	private String command;

	/**
	 * JSON string representing the match
	 */
	@Length(max = 1024)
	private String match;

	/**
	 * Converted JSON object of <code>match</code> JSON string.
	 */
	@Transient
	private transient HookMatch matchObject;


}
