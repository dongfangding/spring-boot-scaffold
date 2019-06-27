package com.ddf.scaffold.fw.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author DDf on 2019/2/15
 */
@Getter
@Setter
@MappedSuperclass
public class CompanyDomain extends BaseDomain {

    @Column(name = "COMP_CODE")
    @Transient
    @TableField(exist = false)
    private String compCode;
}
