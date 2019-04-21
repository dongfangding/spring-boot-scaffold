package com.ddf.scaffold.logic.entity;

import com.ddf.scaffold.fw.entity.CompanyDomain;
import lombok.*;

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
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class User extends CompanyDomain {
    @Column(name = "USER_NAME")
    private String userName;
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "EMAIL")
    private String email;
    @Column(name = "BIRTHDAY")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthDay;
}
