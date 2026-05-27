package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统配置配置属性，负责相关业务逻辑与流程处理。
 */
@Data
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenAiProperties {
    /**
      * 兼容会话补全协议的接口地址。
     */
    private String chatApi = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
      * 服务访问密钥。
     */
    private String apiKey;

    /**
      * 通义千问会话模型名称。
     */
    private String chatModel = "qwen3.6-plus";

    /**
      * 单次回复允许的最大令牌数。
     */
    private Integer chatMaxTokens = 220;
}
