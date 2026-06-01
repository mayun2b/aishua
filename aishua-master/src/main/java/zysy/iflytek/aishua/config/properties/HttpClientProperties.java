package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * HTTP 客户端通用配置。
 */
@Data
@ConfigurationProperties(prefix = "app.http.client")
public class HttpClientProperties {
    /**
     * 建连超时。
     */
    private Duration connectTimeout = Duration.ofSeconds(8);

    /**
     * 读取超时。
     */
    private Duration readTimeout = Duration.ofSeconds(45);
}
