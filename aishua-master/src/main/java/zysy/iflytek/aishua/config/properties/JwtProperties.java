package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统配置配置属性，负责相关业务逻辑与流程处理。
 */
@Data
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    private String secret = "change-me-with-a-32-byte-secret-value";
    private long expirationMs = 86_400_000L;
    private String tokenHeader = "Authorization";
    private String tokenPrefix = "Bearer ";
}
