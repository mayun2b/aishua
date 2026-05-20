package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenAiProperties {
    /**
     * OpenAI compatible chat/completions endpoint.
     */
    private String chatApi = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
     * DashScope API Key.
     */
    private String apiKey;

    /**
     * Qwen chat model name.
     */
    private String chatModel = "qwen3.5-flash";

    /**
     * Maximum tokens for each chat reply.
     */
    private Integer chatMaxTokens = 220;
}
