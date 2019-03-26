package com.ddf.scaffold.fw.entity;

import com.ddf.scaffold.fw.jpa.AuditorAwareImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * &#064;MappedSuperclass 定义实体类所公用属性的超类，映射超类不会生成单独的表，它的映射信息作用于继承自它的实体类。
 * &#064;EntityListeners(AuditingEntityListener.class) 提供对{@code &#064;CreatedDate, &#064;LastModifiedDate}。等注解的支持，
 *      该功能需要依赖{@code spring-aspects}
 * &#064;CreatedBy与&#064;LastModifiedBy的支持请参见 {@link AuditorAwareImpl}
 * @author DDf on 2018/9/29
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BaseDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, updatable = false)
    protected Long id;

    @CreatedBy
    @Column(name = "CREATE_BY")
    protected String createBy;

    @CreatedDate
    @Column(name = "CREATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createTime;

    @LastModifiedBy
    @Column(name = "MODIFY_BY")
    protected String modifyBy;

    @LastModifiedDate
    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modifyTime;

    @Column(name = "REMOVED")
    protected Integer removed = 0;

    @Column(name = "VERSION")
    @Version
    protected Integer version;

}