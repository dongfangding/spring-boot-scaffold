package com.ddf.scaffold.fw.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "P_SERIAL_NO")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class PSerialNo extends BaseDomain implements Serializable {
	private static final long serialVersionUID = 3603749308663591961L;
	@Column(name = "SENO_CURRENT_NO")
	private Long senoCurrentNo;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SENO_EXPIRE")
	private Date senoExpire;
	@Column(name = "SENO_SUFFIX")
	private String senoSuffix;
	@Column(name = "SERU_CODE")
	private String seruCode;
	@Column(name = "SERU_ID")
	private Integer seruId;
	@Column(name = "COMP_CODE")
	private String compCode;

}
