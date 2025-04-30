package com.xxl.tool.test.auth;

import com.xxl.tool.auth.JwtTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtToolTest {
    private static Logger logger = LoggerFactory.getLogger(JwtToolTest.class);

    @Test
    public void test01() {
        // init jwt tool
        String SECRET = "your-256-bit-secret-key-should-be-at-least-32-bytes";
        JwtTool jwtTool = new JwtTool(SECRET);

        // 创建token
        String subject = "user_01";

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("role", "admin");

        String token = jwtTool.createToken(
                subject,
                claims,
                1000 * 60 * 60 * 24  // 24小时过期
        );
        logger.info("Generated Token: " + token);

        // 验证token
        boolean isValid = jwtTool.validateToken(token);
        logger.info("Token is valid: " + isValid);

        // 获取claim
        Object userId = jwtTool.getClaim(token, "userId");
        logger.info("UserId: " + userId);

        // 获取过期时间
        Date expirationTime = jwtTool.getExpirationTime(token);
        logger.info("Expiration Time: " + expirationTime);
    }

    @Test
    public void test02() {
        // init jwt tool
        String SECRET = "your-256-bit-secret-key-should-be-at-least-32-bytes";
        JwtTool jwtTool = new JwtTool(SECRET);

        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyXzAxIiwicm9sZSI6ImFkbWluIiwiZXhwIjoxNzQ2MDYzOTMyLCJpYXQiOjE3NDU5Nzc1MzIsInVzZXJJZCI6IjEyMyJ9.UOscITPd5OThuhe1s61jXwDtvVUOfBWF1E1Ns2sNujs";
        logger.info(""+ jwtTool.validateToken(token));
        logger.info(""+ jwtTool.getClaim(token, "userId"));
    }

}
