package zysy.iflytek.aishua.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import zysy.iflytek.aishua.config.properties.HttpClientProperties;

import java.time.Duration;

/**
 * Shared RestTemplate configuration for external API calls.
 */
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, HttpClientProperties properties) {
        Duration connectTimeout = resolveTimeout(properties.getConnectTimeout(), Duration.ofSeconds(8));
        Duration readTimeout = resolveTimeout(properties.getReadTimeout(), Duration.ofSeconds(45));
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }

    private Duration resolveTimeout(Duration configured, Duration defaultValue) {
        if (configured == null || configured.isNegative() || configured.isZero()) {
            return defaultValue;
        }
        return configured;
    }
}
