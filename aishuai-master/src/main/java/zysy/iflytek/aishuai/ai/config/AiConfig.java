package zysy.iflytek.aishuai.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置类
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    /**
     * 智谱AI配置
     */
    private ZhipuConfig zhipu;

    /**
     * 阿里云qwen3.5-flash配置
     */
    private QwenConfig qwen;

    /**
     * 智谱AI配置类
     */
    @Data
    public static class ZhipuConfig {
        private String embeddingApi;
        private String apiKey;
        private String model;
    }

    /**
     * 阿里云qwen3.5-flash配置类
     */
    @Data
    public static class QwenConfig {
        private String apiUrl;
        private String apiKey;
        private String model;
    }
}
