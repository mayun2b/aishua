package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 配置。
 */
@Data
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    /**
     * 需要应用 CORS 的路径模式。
     */
    private String pathPattern = "/api/**";

    /**
     * 允许的来源模式。
     */
    private List<String> allowedOriginPatterns = List.of("*");

    /**
     * 允许的方法列表。
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * 允许的请求头。
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * 是否允许携带凭证。
     */
    private boolean allowCredentials = false;

    /**
     * 预检缓存秒数。
     */
    private long maxAgeSeconds = 3600L;
}
