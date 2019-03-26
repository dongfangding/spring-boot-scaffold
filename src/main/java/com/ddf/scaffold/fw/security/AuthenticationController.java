package com.ddf.scaffold.fw.security;

import com.ddf.scaffold.fw.util.HInfo;
import com.ddf.scaffold.logic.entity.User;
import com.ddf.scaffold.logic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Value("${jwt.header}")
    private String tokenHeader;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    @Qualifier("jwtUserDetailsService")
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 登陆验证
     *
     * @param authenticationRequest
     * @return
     * @throws AuthenticationException
     */
    @RequestMapping(value = "USER_L", method = RequestMethod.POST)
    public String createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest,
                                           HttpServletRequest request) throws AuthenticationException {
        // 校验用户名称是否存在
        final UserDetails userDetails;
        final User user;
        try {
            user = userRepository.getUserByLoginName(authenticationRequest.getUsername());
        } catch (UsernameNotFoundException e) {
            throw new AuthenticationException(e.getMessage(), e);
        }

        // 验证身份
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
//        if (user.getStatus() != 1) {
//            throw new GlobalCustomizeException(GlobalExceptionEnum.ACCOUNT_DISABLED);
//        }
        userDetails = JwtUserFactory.create(user);
//        if (user.getStatus() == 0) {
//            userDetails = JwtUserFactory.create(user);
//        } else {
//            throw new GlobalCustomizeException(GlobalExceptionEnum.PERMISSION_DENIED);
//        }
        // 生成token
        final String token = jwtTokenUtil.generateToken(userDetails, request);
        // 回写登陆信息
//		user.setLastLoginDate(new Date());
//		user.setLastLoginIp(jwtTokenUtil.getIp(request));
//		userRepository.save(user);
        // 返回
        return token;
    }

    /**
     * 重新生成token
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
    public HInfo refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String authToken = request.getHeader(tokenHeader);
        final String token = authToken.substring(7);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return HInfo.ok(refreshedToken);
        } else {
            return new HInfo("error", "Bad request", null);
        }
    }

    /**
     * 对用户进行身份验证
     * <p>
     */
    private void authenticate(String username, String password) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new AuthenticationException("用户没有激活!", e);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("用户密码信息错误!", e);
        }
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({AuthenticationException.class})
    public HInfo handleAuthenticationException(AuthenticationException e) {
        return HInfo.errorException(e.getMessage());
    }
}
