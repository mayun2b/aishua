package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dify 集成配置。
 */
@Data
@ConfigurationProperties(prefix = "dify")
public class DifyProperties {
    /**
     * Dify 服务地址。
     */
    private String baseUrl;

    /**
     * Dify 应用 API Key。
     */
    private String apiKey;

    /**
     * 工作流执行路径。
     */
    private String workflowRunPath = "/v1/workflows/run";

    /**
     * 请求响应模式。
     */
    private String responseMode = "blocking";
}
