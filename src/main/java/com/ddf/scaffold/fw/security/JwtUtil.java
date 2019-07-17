package com.ddf.scaffold.fw.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * @author dongfang.ding
 * @date 2019/7/17 16:00
 */
public class JwtUtil {

    /**
     * 生成密钥对对象，服务端不保存，每次重启后重新生成，以往数据失效
     */
    private static final KeyPair KEY_PAIR;
    /**
     * 公钥
     */
    private static final PublicKey publicKey;
    /**
     * 私钥
     */
    private static final PrivateKey PRIVATE_KEY;

    /**
     * 生成与解析jws如果不是同一台机器可能会存在时钟差的问题
     * 而导致jws失效，这里提供一个忽略值
     */
    private static final int ALLOWED_CLOCK_SKEW_SECONDS = 60;

    static {
        KEY_PAIR = Keys.keyPairFor(SignatureAlgorithm.RS256);
        publicKey = KEY_PAIR.getPublic();
        PRIVATE_KEY = KEY_PAIR.getPrivate();
    }


    /**
     * 创建默认的Jws payload
     *
     * @param userClaim 传入自定义的数据即可
     * @return
     */
    public static String defaultJws(UserClaim userClaim) {
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, 2);
        calendar.getTime();

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put(UserClaim.USER_ID, userClaim.getUserId());
        claimMap.put(UserClaim.USER_NAME, userClaim.getUserName());
        return Jwts.builder()
                .addClaims(claimMap)
                .setId(UUID.randomUUID().toString())
                .setIssuer("yk-pay")
                .setExpiration(calendar.getTime())
                .setIssuedAt(now)
                .signWith(KEY_PAIR.getPrivate()).compact();
    }

    /**
     * 创建jws
     *
     * @param claims
     * @return
     */
    public static String createJws(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(KEY_PAIR.getPrivate()).compact();
    }

    public static Jws<Claims> parseJws(String jws) {
        Jws<Claims> claimsJws;
        try {
            return Jwts.parser()
                    .setAllowedClockSkewSeconds(ALLOWED_CLOCK_SKEW_SECONDS)
                    .setSigningKey(KEY_PAIR.getPublic())
                    .parseClaimsJws(jws);
        } catch (JwtException ex) {
            throw new RuntimeException("token解析失败");
        }
    }

    public static void main(String[] args) {
        UserClaim userClaim = new UserClaim(1L, "ddf");
        String jws = defaultJws(userClaim);
        Jws<Claims> claimsJws = parseJws(jws);
        String userId = claimsJws.getBody().get("userId") + "";
        String userName = claimsJws.getBody().get("userName") + "";
        System.out.println("userId = " + userId);
        System.out.println("userName = " + userName);
    }
}
