package com.ddf.scaffold.logic.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ddf.scaffold.fw.entity.BaseDomain;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author {@link com.ddf.scaffold.fw.entity.EntityGenerateUtil} Thu May 23 10:42:04 CST 2019
 */
@Entity
@Table(name = "USER_ORDER")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor 
@Data
@Accessors(chain = true)
@TableName(value = "USER_ORDER")
public class UserOrder extends BaseDomain {

    /** 用户id*/
    @Column(name = "USER_ID")
    private Long userId;

    /** 商品名称*/
    @Column(name = "NAME")
    private String name;

    /** 商品数量*/
    @Column(name = "NUM")
    private Integer num;

    /** 单价*/
    @Column(name = "PRICE")
    private Double price;
}