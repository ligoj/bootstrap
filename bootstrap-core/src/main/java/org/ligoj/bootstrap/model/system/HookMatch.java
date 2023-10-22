/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * Hook descriptor.
 */
@JsonIgnoreProperties
@Getter
@Setter
public class HookMatch implements Serializable {
	private String path;
}