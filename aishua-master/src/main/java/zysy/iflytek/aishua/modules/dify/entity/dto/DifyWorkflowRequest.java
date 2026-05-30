package zysy.iflytek.aishua.modules.dify.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Dify 工作流运行接口请求体。
 * 对应 POST /v1/workflows/run。
 */
@Data
public class DifyWorkflowRequest {

    /**
     * 工作流输入变量。
     */
    private Map<String, Object> inputs;

    /**
     * 响应模式（如 blocking / streaming）。
     */
    @JsonProperty("response_mode")
    private String responseMode;

    /**
     * 平台会话用户标识。
     */
    private String user;

}
