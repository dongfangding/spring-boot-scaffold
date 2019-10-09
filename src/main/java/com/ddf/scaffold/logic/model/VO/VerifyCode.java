package com.ddf.scaffold.logic.model.VO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * 验证码返回对象
 *
 * @author dongfang.ding
 * @date 2019/10/9 11:37
 */
@Data
@AllArgsConstructor
@ApiModel("验证码对象")
public class VerifyCode {

    @ApiModelProperty("验证码")
    private String img;

    @ApiModelProperty("随机码，生成验证码的时候返回给前端，配合验证码一起两个参数都要传递给登录接口")
    private String uuid;
}
