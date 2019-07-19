package com.ddf.scaffold.fw.security;

import com.ddf.scaffold.logic.model.entity.User;
import com.ddf.scaffold.logic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("loginName", username);
        User user = userRepository.findOneByProperties(propertiesMap);

        if (user == null) {
            throw new UsernameNotFoundException(String.format("用户名不存在 '%s'.", username));
        } else {
            return JwtUserFactory.create(user);
        }
    }
}
