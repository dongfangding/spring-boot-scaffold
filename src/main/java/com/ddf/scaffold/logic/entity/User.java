package com.ddf.scaffold.logic.entity;

import com.ddf.scaffold.fw.entity.CompanyDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * 
 * @author {@link com.ddf.scaffold.fw.entity.EntityGenerateUtil} Tue May 28 14:47:48 CST 2019
 */
@Entity
@Table(name = "USER")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor 
@Data
@ApiModel("用户")
public class User extends CompanyDomain {

    /** 主键ID*/
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;

    /** 姓名*/
    @ApiModelProperty("姓名")
    @Column(name = "USER_NAME")
    private String userName;

    /** 密码*/
    @ApiModelProperty("密码")
    @Column(name = "PASSWORD")
    private String password;

    /** 邮箱*/
    @ApiModelProperty("邮箱")
    @Column(name = "EMAIL")
    private String email;

    /** 生日*/
    @ApiModelProperty("生日")
    @Column(name = "BIRTHDAY")
    @Temporal(TemporalType.DATE)
    private Date birthday;
}