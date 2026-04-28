package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    private String secret = "change-me-with-a-32-byte-secret-value";
    private long expirationMs = 86_400_000L;
    private String tokenHeader = "Authorization";
    private String tokenPrefix = "Bearer ";
}
