package com.ddf.scaffold.fw.security;

import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录的用户
 *
 * @author dongfang.ding
 * @date 2019/9/17 10:02
 */
public class SecurityUtils {

    public static UserClaim getUserDetails() {
        UserClaim userClaim;
        try {
            userClaim = (UserClaim) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.LOGIN_EXPIRED);
        }
        return userClaim;
    }

    /**
     * 获取当前用户名称
     *
     * @return 系统用户名称
     */
    public static String getUsername() {
        UserClaim userClaim = getUserDetails();
        return userClaim.getUsername();
    }

    /**
     * 获取当前用户id
     *
     * @return 系统用户id
     */
    public static Long getUserId() {
        UserClaim userClaim = getUserDetails();
        return userClaim.getUserId();
    }


    /**
     * 获取当前用户所属组织代码
     *
     * @return 当前用户所属组织代码
     */
    public static String getUserOrgCode() {
        UserClaim userClaim = getUserDetails();
        return userClaim.getOrgCode();
    }


}
