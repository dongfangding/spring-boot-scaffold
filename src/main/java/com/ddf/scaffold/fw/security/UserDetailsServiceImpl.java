package com.ddf.scaffold.fw.security;

import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import com.ddf.scaffold.fw.exception.GlobalExceptionEnum;
import com.ddf.scaffold.fw.util.WebUtil;
import com.ddf.scaffold.logic.model.entity.BootUser;
import com.ddf.scaffold.logic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * 加载UserDetails用户信息
 *
 * @author dongfang.ding
 * @date 2019/9/16 10:23
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username){
        BootUser bootUser = userService.findByName(username);
        if (bootUser == null) {
            throw new GlobalCustomizeException(GlobalExceptionEnum.INVALID_ACCOUNT);
        } else {
            return createJwtUser(bootUser);
        }
    }

    private UserDetails createJwtUser(BootUser bootUser) {
        return new UserClaim(
                bootUser.getId(),
                bootUser.getUserName(),
                WebUtil.getHost(),
                bootUser.getLastModifyPassword(),
                bootUser.getIsEnable() == 1,
                bootUser.getOrgCode()
        );
    }
}
