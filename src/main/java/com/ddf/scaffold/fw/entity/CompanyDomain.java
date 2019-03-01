package com.ddf.scaffold.fw.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author DDf on 2019/2/15
 */
@Getter
@Setter
@MappedSuperclass
public class CompanyDomain extends BaseDomain {

    @Column(name = "COMP_CODE")
    private String compCode;
}
