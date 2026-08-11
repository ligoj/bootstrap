/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ligoj.bootstrap.core.model.AbstractAudited;

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
