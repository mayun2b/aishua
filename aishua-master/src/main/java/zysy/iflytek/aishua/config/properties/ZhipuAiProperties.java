package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.zhipu")
public class ZhipuAiProperties {
    /**
     * 智谱向量接口，保留原有配置使用�?     */
    private String embeddingApi;

    /**
     * 智谱聊天接口，默认使�?OpenAPI 兼容 chat/completions�?     */
    private String chatApi = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * 智谱 API Key�?     */
    private String apiKey;

    /**
     * 向量模型，保留原有配置�?     */
    private String model = "embedding-2";

    /**
     * 聊天主模型�?     */
    private String chatModel = "qwen3.5-plus-2026-02-15";

    /**
     * 单次聊天回复�?completion token 上限，用于控制成本�?     */
    private Integer chatMaxTokens = 220;
}
