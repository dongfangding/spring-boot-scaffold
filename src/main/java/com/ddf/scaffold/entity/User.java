package com.ddf.scaffold.entity;

import com.ddf.scaffold.fw.entity.BaseDomain;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @author DDf on 2018/12/1
 */
@Entity
@Table(name = "USER")
@ToString(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class User extends BaseDomain {
    @Column(name = "USER_NAME")
    private String userName;
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "BIRTHDAY")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthDay;
    @Transient
    private String validateEmail;
    @Transient
    private String confirmPassword;
    @Transient
    private String oldPassword;
}
