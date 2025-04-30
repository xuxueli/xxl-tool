package com.xxl.tool.auth;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * jwt tool
 *
 * @author xuxueli 2025-04
 */
public class JwtTool {

    /**
     * jwt 签名密钥
     */
    private final JWSSigner signer;
    private final JWSVerifier verifier;

    public JwtTool(String secret) {
        try {
            this.signer = new MACSigner(secret);
            this.verifier = new MACVerifier(secret);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }
    public JwtTool(JWSSigner signer, JWSVerifier verifier) {
        this.signer = signer;
        this.verifier = verifier;
    }


    /**
     * 创建 JWT token
     *
     * @param subject   主题, 通常为用户标识
     * @param claims    自定义声明, 信息键值对
     * @param ttlMillis 过期时间（毫秒）
     */
    public String createToken(String subject, Map<String, Object> claims, long ttlMillis) {
        try {
            // 1、JWT声明
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + ttlMillis));
            // 添加自定义声明
            if (claims != null) {
                claims.forEach(builder::claim);
            }

            // 2、已签名的JWT：header + payload + signature
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).build(),
                    builder.build()
            );

            // 3、JWT签名器
            //JWSSigner signer = new MACSigner(secret);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("创建JWT token失败", e);
        }
    }

    /**
     * 验证 JWT token 合法性
     *
     * @param token 待验证的token
     */
    public boolean validateToken(String token) {
        try {
            // 1、解析JWT
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 2、HMAC 验证器
            //JWSVerifier verifier = new MACVerifier(secret);

            // 3、验证签名
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            // 4、验证过期时间
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && expirationTime.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 JWT token 声明信息
     *
     * @param token 待解析的token
     */
    public JWTClaimsSet parseToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet();
    }

    /**
     * 获取指定 claim 信息
     *
     * @param token     待解析的token
     * @param claimName claim名称
     */
    public Object getClaim(String token, String claimName) {
        try {
            JWTClaimsSet claims = parseToken(token);
            return claims.getClaim(claimName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取过期时间
     *
     * @param token 待解析的token
     */
    public Date getExpirationTime(String token) {
        try {
            JWTClaimsSet claims = parseToken(token);
            return claims.getExpirationTime();
        } catch (Exception e) {
            return null;
        }
    }

}