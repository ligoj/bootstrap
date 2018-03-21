/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

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
	@Column(name = "PICTURE", length = 5000000)
	@Basic(fetch = FetchType.LAZY)
	private byte[] picture;

}
