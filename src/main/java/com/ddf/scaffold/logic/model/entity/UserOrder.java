package com.ddf.scaffold.logic.model.entity;

import com.ddf.scaffold.fw.entity.BaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * @author {@link com.ddf.scaffold.fw.entity.EntityGenerateUtil} Tue May 28 15:32:18 CST 2019
 */
@Entity
@Table(name = "boot_user_order")
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor 
@Data
@ApiModel("用户订单")
@Accessors(chain = true)
public class UserOrder extends BaseDomain {

    /** 用户id*/
    @ApiModelProperty("用户id")
    @Column(name = "USER_ID")
    private Long userId;

    /** 商品名称*/
    @ApiModelProperty("商品名称")
    @Column(name = "NAME")
    private String name;

    /** 商品数量*/
    @ApiModelProperty("商品数量")
    @Column(name = "NUM")
    private Integer num;

    /** 单价*/
    @ApiModelProperty("单价")
    @Column(name = "PRICE")
    private Double price;
}