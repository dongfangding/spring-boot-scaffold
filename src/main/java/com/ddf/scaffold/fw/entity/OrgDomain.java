package com.ddf.scaffold.fw.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author dongfang.ding on 2019/2/15
 */
@Getter
@Setter
@MappedSuperclass
public class OrgDomain extends BaseDomain {

    @Column(name = "ORG_CODE")
    @Transient
    private String orgCode;
}
