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
 * JWT 工具：基于 nimbus-jose-jwt 创建、校验与解析 JWT
 *
 * <p>可选依赖：{@code com.nimbusds:nimbus-jose-jwt}（作用域 {@code provided}），
 * 未引入该依赖时本类无法加载。</p>
 *
 * <p><b>签名算法：</b>使用 {@link JWSAlgorithm#HS256}（HMAC-SHA256）对称签名。
 * 构造时传入的密钥长度需至少 32 字节（256 位），否则 {@link MACSigner} / {@link MACVerifier}
 * 会因密钥过短拒绝初始化。</p>
 *
 * <p><b>过期校验：</b>{@link #validateToken(String)} 会校验签名与 {@code exp}（必须存在且晚于当前时间），
 * 但不校验 {@code nbf}、{@code iss}、{@code aud}。{@link #parseToken(String)}、
 * {@link #getClaim(String, String)}、{@link #getExpirationTime(String)} <b>只解析不验签</b>，
 * 不可用于信任判定；需可信时必须先调用 {@code validateToken}。</p>
 *
 * <p><b>安全警告：</b>密钥必须使用高强度随机值并通过安全渠道管理，禁止硬编码到源码；
 * token 默认只做 Base64URL 编码，不应放置密码等敏感信息。校验失败统一返回
 * {@code false}/{@code null}，不抛出细节，避免泄露攻击信息。</p>
 *
 * <p><b>线程安全：</b>实例不可变（字段 {@code final}），Nimbus 的 signer/verifier 为无状态实现，
 * 实例可安全复用；方法不修改入参 {@code claims}。</p>
 *
 * @author xuxueli 2025-04
 */
public class JwtTool {

    /**
     * jwt 签名器
     */
    private final JWSSigner signer;
    /**
     * jwt 签名校验器
     */
    private final JWSVerifier verifier;

    /**
     * 使用对称密钥创建工具
     *
     * @param secret HMAC 密钥，HS256 要求至少 32 字节
     * @throws RuntimeException 密钥不合法导致 {@link MACSigner} / {@link MACVerifier} 初始化失败时抛出
     */
    public JwtTool(String secret) {
        try {
            this.signer = new MACSigner(secret);
            this.verifier = new MACVerifier(secret);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用自定义签名器与校验器创建工具
     *
     * @param signer   签名器，不可为 {@code null}
     * @param verifier 签名校验器，不可为 {@code null}
     */
    public JwtTool(JWSSigner signer, JWSVerifier verifier) {
        this.signer = signer;
        this.verifier = verifier;
    }


    /**
     * 创建 JWT token
     *
     * <p>使用 HS256 签名，自动写入 {@code sub}、{@code iat}、{@code exp}。</p>
     *
     * @param subject   主题, 通常为用户标识
     * @param claims    自定义声明, 信息键值对，{@code null} 表示不附加
     * @param ttlMillis 过期时间（毫秒），从当前时间起算；{@code <= 0} 会生成立即过期的 token
     * @return 序列化后的 JWT 字符串
     * @throws RuntimeException 签名失败（如密钥不可用）时抛出
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
     * <p>依次校验签名与过期时间；不校验 {@code nbf}、签发方与受众。任何解析或校验异常均返回
     * {@code false}，不会抛出。</p>
     *
     * @param token 待验证的token，可为 {@code null}
     * @return 签名有效且未过期返回 {@code true}，否则返回 {@code false}
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
     * <p><b>安全提示：</b>仅解析 payload，<b>不验证签名与过期时间</b>；不可将解析结果直接视为可信。</p>
     *
     * @param token 待解析的token
     * @return JWT 声明集合
     * @throws ParseException token 格式非法时抛出
     */
    public JWTClaimsSet parseToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet();
    }

    /**
     * 获取指定 claim 信息
     *
     * <p><b>安全提示：</b>只解析不验签，需确认 token 可信时先调用 {@link #validateToken(String)}。</p>
     *
     * @param token     待解析的token
     * @param claimName claim名称
     * @return claim 值；token 非法、解析失败或不存在该 claim 时返回 {@code null}
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
     * <p><b>安全提示：</b>只解析不验签，不能据此判断 token 是否有效。</p>
     *
     * @param token 待解析的token
     * @return 过期时间；token 非法、解析失败或未设置 {@code exp} 时返回 {@code null}
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