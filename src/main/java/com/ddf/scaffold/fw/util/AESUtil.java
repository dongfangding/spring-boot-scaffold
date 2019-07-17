package com.ddf.scaffold.fw.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.Base64;


/**
 * AES for java AES/ECB/PKCS5Padding
 *
 * @author kevin(askyiwang @ gmail.com)
 * @date 2016年10月25日22:27:21
 */
public class AESUtil {
    /**
     * AES/CBC/PKCS7Padding的模式
     */
    private final String CIPHERMODEPADDING = "AES/CBC/PKCS5Padding";

    private SecretKeySpec skforAES = null;
    /**
     * 密钥初始向量，128位16个字节
     */
    private static String ivParameter = "";

    private byte[] iv = ivParameter.getBytes();
    private IvParameterSpec IV;
    /**
     * 秘钥， 128位16个字节
     */
    private static final String private_key = "huangxiaoguo1234";

    private static AESUtil instance = null;

    public static AESUtil getInstance() {
        if (instance == null) {
            synchronized (AESUtil.class) {
                if (instance == null) {
                    instance = new AESUtil();
                }
            }
        }
        return instance;
    }

    public AESUtil() {
        byte[] skAsByteArray;
        try {
            skAsByteArray = private_key.getBytes("ASCII");
            skforAES = new SecretKeySpec(skAsByteArray, "AES");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        IV = new IvParameterSpec(iv);
    }

    public String encrypt(byte[] plaintext) {
        byte[] ciphertext = encrypt(CIPHERMODEPADDING, skforAES, IV, plaintext);
        String base64_ciphertext = new String(Base64.getEncoder().encode(ciphertext));
        return base64_ciphertext;
    }

    public String decrypt(String ciphertext_base64) {
        byte[] s = Base64.getDecoder().decode(ciphertext_base64);
        String decrypted = new String(decrypt(CIPHERMODEPADDING, skforAES, IV, s));
        return decrypted;
    }

    private byte[] encrypt(String cmp, SecretKey sk, IvParameterSpec IV, byte[] msg) {
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.ENCRYPT_MODE, sk, IV);
            return c.doFinal(msg);
        } catch (Exception nsae) {
        }
        return null;
    }

    private byte[] decrypt(String cmp, SecretKey sk, IvParameterSpec IV,
                           byte[] ciphertext) {
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.DECRYPT_MODE, sk, IV);
            return c.doFinal(ciphertext);
        } catch (Exception nsae) {
        }
        return null;
    }
}