/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import org.ligoj.bootstrap.core.model.AbstractAudited;

import lombok.Getter;
import lombok.Setter;

/**
 * System performance entity.
 */
@Entity
@Table(name = "S_BENCH")
@Getter
@Setter
public class SystemBench extends AbstractAudited<Integer> {

	@Column(name = "PRF_BOOL")
	private Boolean prfBool;

	@Column(name = "PRF_CHAR", length = 50)
	private String prfChar;

	@Lob
	@Column(name = "PICTURE", length = 10000000)
	@Basic(fetch = FetchType.LAZY)
	private byte[] picture;

}
