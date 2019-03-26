package com.ddf.scaffold.fw.security;

import com.ddf.scaffold.fw.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserToken {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public static String getUserName() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userDetails.getUsername();
        } catch (Exception e) {
            return ConstUtil.ANONYMOUS_NAME;
        }

    }

    public static JwtUser getJwtUser() {
        return (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static String getCompCode() {
        return getJwtUser().getUser().getCompCode();
    }

    public String getOrgCode() {
        return jwtTokenUtil.getClaim("orgCode");
    }

    public String getOrgName() {
        return jwtTokenUtil.getClaim("orgName");
    }

    public Byte getOrgType() {
        Byte orgType = Byte.parseByte(jwtTokenUtil.getClaim("orgType"));
        return orgType;
    }

    public String getLastLoginIp() {
        return jwtTokenUtil.getClaim("lastLoginIp");
    }

    public String getOrgCodes() {
        return jwtTokenUtil.getClaim("orgCodes");
    }
}
