package com.ddf.scaffold.fw.security;


import com.ddf.scaffold.fw.exception.GlobalCustomizeException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义jws的payload部分的用户信息类
 * 生成Jwt的时候调用参数为该类的方法即可，会把这个类中的所有有get方法的字段都生成到payload中
 *
 * @author dongfang.ding
 * @date 2019/7/17 17:46
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserClaim implements Serializable, UserDetails {
    private static final long serialVersionUID = -6557510720376811244L;

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_NAME = "userName";
    private static final String HEADER_CHARSET = "UTF-8";

    /**
     * 默认用户信息
     * 判断是否是默认用户
     * <pre class="code">
     *     UserClaim userClaim = WebUtil.getUserClaim();
     *     if (userClaim == UserClaim.DEFAULT_USER) {
     *         // 说明没有用户信息
     *     }
     * </pre>
     */
    private static final UserClaim DEFAULT_USER = new UserClaim(0L, "");

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户登录的授信设备唯一标识符
     * 每一次签发token 都必须包含当前登录的设备标识，需要维护每个用户已签发的设备标识,如果
     * 用未签发过token的设备标识发送认证信息，服务器会拒绝认证
     * <p>
     * 这个标识的规则，最好是服务端不论客户端是什么环境，都能获取到的一个值，比如ip，当然ip只是
     * 符合条件，但却不是最好的方案，暂时还没想到更好的方案
     */
    private String credit;

    /**
     * 最后一次修改密码的时间，签发token时设置值；解析token时如果这个值与数据库最后一次修改密码的时间不匹配，
     * 证明密码已被修改，则该token校验不通过
     * 注意： 如果有其它字段也代表用户有效性变化的字段，那么当这些字段变化的时候也应该修改这个值；比如用户被删除，或者用户账号被修改之类的
     */
    private Long lastModifyPasswordTime;

    /**
     * 预留备注字段
     */
    private String remarks;

    /**
     * 用户是否启用
     */
    private boolean enable;

    /**
     * 用户所属组织代码
     */
    private String orgCode;


    public UserClaim(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UserClaim(Long userId, String username, String credit, long lastModifyPasswordTime, boolean enable, String orgCode) {
        this.userId = userId;
        this.username = username;
        this.credit = credit;
        this.lastModifyPasswordTime = lastModifyPasswordTime;
        this.enable = enable;
        this.orgCode = orgCode;
    }

    /**
     * 返回默认用户，无用户信息
     *
     * @return
     * @see UserClaim#defaultUser
     */
    public static UserClaim defaultUser() {
        return DEFAULT_USER;
    }

    /**
     * 将用户信息生成map，用以放到jwt的payload中
     *
     * @return
     */
    public Map<String, Object> toMap() {
        Map<String, Object> claimMap = new HashMap<>();
        Class<? extends UserClaim> aClass = this.getClass();
        Field[] fields = aClass.getDeclaredFields();
        if (fields.length > 0) {
            for (Field field : fields) {
                try {
                    Method method = aClass.getDeclaredMethod("get" + field.getName().substring(0, 1)
                            .toUpperCase() + field.getName().substring(1));
                    // 如果能找到方法就设置
                    claimMap.put(field.getName(), method.invoke(this));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    // 没有方法的直接忽略掉
                }
            }
        }
        return claimMap;
    }

    /**
     * 将用户部分信息放入到请求头中，在这里统一将需要放入请求头的放入到Map中
     *
     * @return
     */
    public Map<String, Object> toHeader() {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put(CLAIM_USER_ID, this.getUserId());
        try {
            claimMap.put(CLAIM_USER_NAME, URLEncoder.encode(this.username, HEADER_CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new GlobalCustomizeException("不支持的编码");
        }
        return claimMap;
    }

    /**
     * Returns the authorities granted to the bootUser. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * Returns the password used to authenticate the bootUser.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Returns the username used to authenticate the bootUser. Cannot return <code>null</code>.
     *
     * @return the username (never <code>null</code>)
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the bootUser's account has expired. An expired account cannot be
     * authenticated.
     *
     * @return <code>true</code> if the bootUser's account is valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the bootUser is locked or unlocked. A locked bootUser cannot be
     * authenticated.
     *
     * @return <code>true</code> if the bootUser is not locked, <code>false</code> otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the bootUser's credentials (password) has expired. Expired
     * credentials prevent authentication.
     *
     * @return <code>true</code> if the bootUser's credentials are valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the bootUser is enabled or disabled. A disabled bootUser cannot be
     * authenticated.
     *
     * @return <code>true</code> if the bootUser is enabled, <code>false</code> otherwise
     */
    @Override
    public boolean isEnabled() {
        return enable;
    }
}