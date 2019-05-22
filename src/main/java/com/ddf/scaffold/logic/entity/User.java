package com.ddf.scaffold.logic.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ddf.scaffold.fw.entity.CompanyDomain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * @author DDf on 2018/12/1
 */
@Entity
@Table(name = "USER")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName(value = "USER")
public class User extends CompanyDomain {
    @Column(name = "USER_NAME")
    @TableField(value = "USER_NAME")
    private String userName;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "BIRTHDAY")
    @Temporal(TemporalType.TIMESTAMP)
    @TableField(value = "BIRTHDAY")
    private Date birthDay;

    @Transient
    @TableField(exist = false)
    private String compCode;

}
