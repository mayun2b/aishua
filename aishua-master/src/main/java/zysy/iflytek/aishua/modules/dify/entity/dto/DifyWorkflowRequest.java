package zysy.iflytek.aishua.modules.dify.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 流程编排工作流调用请求体。
 */
@Data
public class DifyWorkflowRequest {

    /**
     * 工作流输入变量。
     */
    private Map<String, Object> inputs;

    /**
     * 平台要求的响应模式（流式或阻塞）。
     */
    @JsonProperty("response_mode")
    private String responseMode;

    /**
     * 平台会话用户标识。
     */
    private String user;

}
