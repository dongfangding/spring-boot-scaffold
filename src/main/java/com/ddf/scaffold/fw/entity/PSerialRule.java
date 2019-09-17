package com.ddf.scaffold.fw.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Table(name = "P_SERIAL_RULE")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class PSerialRule extends OrgDomain implements Serializable {
	private static final long serialVersionUID = 6275470964335796126L;
	/** 业务号规则代码 */
	@Column(name = "SERU_CODE")
	private String seruCode;
	/** 业务规则循环周期 */
	@Column(name = "SERU_LOOP_PERIOD")
	private Byte seruLoopPeriod;
	/** 业务号规则名称 */
	@Column(name = "SERU_NAME")
	private String seruName;
	/** 业务号规则 */
	@Column(name = "SERU_RULE")
	private String seruRule;
	/** 业务号序列号长度 */
	@Column(name = "SERU_SN_LENGTH")
	private Integer seruSnLength;
	/** 业务号规则后缀 */
	@Column(name = "SERU_UNIQ_SUFFIX")
	private String seruUniqSuffix;
	/** 起始序号 */
	@Column(name = "SERU_START_NO")
	private Integer seruStartNo;
}
