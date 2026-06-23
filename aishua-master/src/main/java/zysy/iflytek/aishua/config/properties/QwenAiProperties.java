package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Qwen AI service configuration.
 */
@Data
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenAiProperties {
    /**
     * Chat completion endpoint.
     */
    private String chatApi = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
     * Provider API key.
     */
    private String apiKey;

    /**
     * Default chat model.
     */
    private String chatModel = "qwen3.6-plus";

    /**
     * Default max tokens for a single completion.
     */
    private Integer chatMaxTokens = 220;

    /**
     * Streaming connection timeout.
     */
    private Duration streamConnectTimeout = Duration.ofSeconds(8);

    /**
     * Streaming request timeout.
     */
    private Duration streamRequestTimeout = Duration.ofSeconds(80);

    /**
     * Multimodal grading model.
     */
    private String gradingModel = "qwen-vl-max";

    /**
     * Max tokens for grading JSON output.
     */
    private Integer gradingMaxTokens = 800;

    /**
     * Request timeout for grading calls.
     */
    private Duration gradingTimeout = Duration.ofSeconds(90);

    /**
     * Number of pending grading tasks processed in one scheduler tick.
     */
    private Integer gradingBatchSize = 3;

    /**
     * Max images sent to the grading model for one answer.
     */
    private Integer gradingMaxImages = 6;
}
