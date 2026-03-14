package zysy.iflytek.aishuai.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import zysy.iflytek.aishuai.common.constant.JwtConstants;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT生成/解析工具类
 * 适配 JJWT 0.11.5 版本
 */
@Slf4j
public class JwtUtil {

    // 生成Token（根据用户ID）
    public static String generateToken(Long userId) {
        try {
            // 校验密钥长度（HS256要求密钥至少256位/32字符）
            SecretKey key = getSecretKey();

            return Jwts.builder()
                    .setSubject(userId.toString()) // 存储用户ID
                    .setIssuedAt(new Date()) // 签发时间
                    .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.EXPIRATION)) // 过期时间
                    .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                    .compact();
        } catch (Exception e) {
            log.error("生成JWT Token失败", e);
            throw new RuntimeException("生成登录凭证失败，请重试");
        }
    }

    // 从Token解析用户ID
    public static Long getUserIdFromToken(String token) {
        try {
            SecretKey key = getSecretKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (JwtException e) { // 统一捕获所有JWT相关异常
            log.error("解析JWT Token失败: {}", e.getMessage(), e);
            return null;
        } catch (NumberFormatException e) {
            log.error("Token中用户ID格式错误", e);
            return null;
        } catch (Exception e) {
            log.error("解析JWT Token发生未知错误", e);
            return null;
        }
    }

    // 验证Token是否有效
    public static boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空，验证失败");
            return false;
        }

        try {
            SecretKey key = getSecretKey();
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token已过期: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Token签名无效: {}", e.getMessage());
        } catch (JwtException e) { // 捕获所有JWT相关异常（替代InvalidJwtException）
            log.error("Token无效: {}", e.getMessage());
        } catch (Exception e) {
            log.error("验证Token失败: {}", e.getMessage(), e);
        }
        return false;
    }

    // 封装密钥获取逻辑，统一处理密钥长度问题
    private static SecretKey getSecretKey() {
        byte[] keyBytes = JwtConstants.SECRET.getBytes(StandardCharsets.UTF_8);
        // 校验密钥长度（HS256要求至少32字节，这里给出友好提示）
        if (keyBytes.length < 32) {
            log.error("JWT密钥长度不足32字符，当前长度: {}", keyBytes.length);
            throw new IllegalArgumentException("JWT密钥长度必须至少32个字符");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}