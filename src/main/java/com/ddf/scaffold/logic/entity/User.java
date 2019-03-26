package com.ddf.scaffold.logic.entity;

import com.ddf.scaffold.fw.entity.CompanyDomain;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

/**
 * @author DDf on 2018/12/1
 */
@Entity
@Table(name = "USER")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class User extends CompanyDomain {
    /** 用户类型 0：平台用户  1：会员用户 */
    @Column(name = "USER_TYPE")
    private Byte userType;

    /** 登录名 */
    @Column(name = "USER_LOGIN_NAME")
    private String loginName;

    /** 密码 */
    @Column(name = "USER_PASSWORD")
    private String password;

    /** 姓名 */
    @Column(name = "USER_NAME")
    private String userName;

    /** 电话 */
    @Column(name = "USER_TEL")
    private String userTel;

    /** 手机 */
    @Column(name = "USER_MOBILE")
    private String userMobile;

    /** 传真 */
    @Column(name = "USER_FAX")
    private String userFax;

    /** EMAIL */
    @Column(name = "USER_EMAIL")
    private String userEmail;

    /** QQ */
    @Column(name = "USER_QQ")
    private String userQQ;

    /** 图像地址 */
    @Column(name = "USER_ICONS")
    private String userIcons;

    /** 部门 */
    @Column(name = "DEPARTMENT")
    private String department;

    /** 职务 */
    @Column(name = "TITLE")
    private String title;

    /** 用户功能点（，分隔） */
    @Column(name = "USER_FUNCTIONS")
    private String userFunctions;

    /** 状态 0：未激活 1：激活 */
    @Column(name = "ACTIVE")
    private Byte active;


    @Transient
    private String oldPassword;

    @Transient
    private String confirmPassword;

    @Transient
    private String validateEmail;
}
