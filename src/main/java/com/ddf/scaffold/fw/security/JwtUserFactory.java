package com.ddf.scaffold.fw.security;


import com.ddf.scaffold.logic.entity.User;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(User user) {
        return new JwtUser(user, user.getUserName(), user.getPassword(), null, true, null);
    }
}
