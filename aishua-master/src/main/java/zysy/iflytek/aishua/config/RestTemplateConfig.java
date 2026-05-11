package zysy.iflytek.aishua.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // AI 调用存在网络波动，这里显式设置超时避免线程长时间阻塞。
                .setConnectTimeout(Duration.ofSeconds(8))
                .setReadTimeout(Duration.ofSeconds(45))
                .build();
    }
}
