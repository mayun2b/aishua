package zysy.iflytek.aishua.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zysy.iflytek.aishua.config.properties.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 令牌工具，负责生成、校验与用户标识解析。
 */
@Slf4j
@Component
public class JwtService {
    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("令牌密钥长度至少为 32 字节");
        }
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 处理当前业务逻辑。
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpirationMs());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 执行参数与状态校验。
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException exception) {
            log.info("JWT expired: {}", exception.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException exception) {
            log.info("JWT invalid: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * 查询并返回处理结果。
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            log.info("JWT parse failed: {}", exception.getMessage());
            return null;
        }
    }
}
