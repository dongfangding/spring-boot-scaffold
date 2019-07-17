package com.ddf.scaffold.fw.security;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义jws的payload部分的用户信息类
 *
 * @author dongfang.ding
 * @date 2019/7/17 17:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClaim {
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";

    private Long userId;
    private String userName;
}