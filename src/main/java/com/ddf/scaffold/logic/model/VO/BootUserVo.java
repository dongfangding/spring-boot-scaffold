package com.ddf.scaffold.logic.model.VO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * 用户展示层对象
 *
 * @author dongfang.ding
 * @date 2019/10/9 11:47
 */
@Data
@ApiModel("用户展示层对象")
@Accessors(chain = true)
public class BootUserVo {

    /** 姓名*/
    @ApiModelProperty("姓名")
    @Column(name = "USER_NAME")
    private String userName;

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
