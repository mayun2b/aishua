package zysy.iflytek.aishua.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 外部集成调用使用的默认请求客户端配置。
 */
@Configuration
public class RestTemplateConfig {
    /**
     * 处理当前业务逻辑。
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // 显式设置超时，避免外部智能服务阻塞导致请求长期挂起。
        return builder
                .setConnectTimeout(Duration.ofSeconds(8))
                .setReadTimeout(Duration.ofSeconds(45))
                .build();
    }
}
