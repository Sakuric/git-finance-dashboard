package com.example.financedashboard.utils;

import com.example.financedashboard.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT工具类
 * JWT（JSON Web Token）是一种开放标准（RFC 7519），用于在网络应用环境间传递声明的基于JSON的令牌
 * 它被设计为紧凑且安全的，特别适用于分布式站点的单点登录（SSO）场景
 * JWT由三部分组成：头部（Header）、载荷（Payload）和签名（Signature）
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret}") // 从配置文件中获取JWT密钥
    private String secret; // JWT密钥,用于生成和验证JWT

    @Value("${jwt.expiration}") // 从配置文件中获取JWT的过期时间
    private Long expiration; // JWT的过期时间,单位为秒
    
    // 获取签名密钥
    private SecretKey getSigningKey() {
        // 使用Base64解码密钥，确保密钥长度足够
        byte[] keyBytes;
        try {
            // 尝试Base64解码
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            // 如果不是Base64编码，直接使用字符串字节
            keyBytes = secret.getBytes();
        }
        
        // 确保密钥至少有256位（32字节）用于HS256算法
        if (keyBytes.length < 32) {
            // 如果密钥长度不足，使用SHA-256哈希扩展到32字节
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(secret.getBytes());
            } catch (java.security.NoSuchAlgorithmException ex) {
                // 如果SHA-256不可用，进行简单填充
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
                keyBytes = paddedKey;
            }
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT令牌
     * @param user 用户信息
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(User user) {
        // 构建JWT令牌
        return Jwts.builder()
                .subject(user.getUsername()) // 设置主题，通常是用户名
                .claim("userId", user.getId()) // 添加自定义声明，存储用户ID
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 设置过期时间
                .signWith(getSigningKey()) // 设置签名密钥
                .compact(); // 生成令牌
    }

    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey()) // 设置密钥
                .build() // 构建解析器
                .parseSignedClaims(token) // 解析JWT令牌
                .getPayload(); // 获取载荷

        // 在载荷中获取主题（返回用户名）
        return claims.getSubject();
    }

    /**
     * 验证JWT令牌是否有效
     * @param token JWT令牌
     * @return true表示令牌有效，false表示令牌无效
     */
    public boolean validateToken(String token) {
        try {
            // 解析JWT令牌，如果解析过程中没有抛出异常，则令牌有效
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 如果解析过程中抛出异常，则令牌无效
            return false;
        }
    }
}
