package com.ddf.scaffold.logic.model.entity;

import com.ddf.scaffold.fw.entity.OrgDomain;
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
@Table(name = "boot_user")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor 
@Data
@ApiModel("用户")
public class BootUser extends OrgDomain {

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

    @ApiModelProperty("最后一次修改密码的时间")
    @Column(name = "LAST_MODIFY_PASSWORD")
    private Long lastModifyPassword;

    @ApiModelProperty("用户是否有效 0 否 1 是")
    @Column(name = "IS_ENABLE")
    private Byte isEnable;


}