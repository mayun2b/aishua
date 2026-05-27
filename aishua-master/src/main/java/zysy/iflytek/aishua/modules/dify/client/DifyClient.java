package zysy.iflytek.aishua.modules.dify.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import zysy.iflytek.aishua.modules.dify.entity.dto.DifyWorkflowRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程编排平台客户端，负责拼装请求并调用工作流接口。
 */
@Component
public class DifyClient {
    private static final String WORKFLOW_RUN_PATH = "/v1/workflows/run";
    private static final String RESPONSE_MODE_BLOCKING = "blocking";
    private static final String DEFAULT_DIFY_USER = "test-user-001";

    private final RestTemplate restTemplate;
    private final String difyBaseUrl;
    private final String difyApiKey;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public DifyClient(
            RestTemplate restTemplate,
            @Value("${dify.base-url}") String difyBaseUrl,
            @Value("${dify.api-key}") String difyApiKey
    ) {
        this.restTemplate = restTemplate;
        this.difyBaseUrl = difyBaseUrl;
        this.difyApiKey = difyApiKey;
    }

    /**
     * 处理当前业务逻辑。
     */
    public Map<String, Object> runWorkflow(String query, String studentId) {
        DifyWorkflowRequest request = buildRequest(query, studentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(difyApiKey);

        HttpEntity<DifyWorkflowRequest> entity = new HttpEntity<>(request, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> response = restTemplate.exchange(
                resolveWorkflowUrl(),
                HttpMethod.POST,
                entity,
                Map.class
        );

        return response.getBody() == null ? Map.of() : response.getBody();
    }

    /**
     * 构建处理所需数据结构。
     */
    private DifyWorkflowRequest buildRequest(String query, String studentId) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("query", query);
        if (StringUtils.hasText(studentId)) {
            inputs.put("student_id", studentId.trim());
        }

        DifyWorkflowRequest request = new DifyWorkflowRequest();
        request.setInputs(inputs);
        request.setResponseMode(RESPONSE_MODE_BLOCKING);
        request.setUser(DEFAULT_DIFY_USER);
        return request;
    }

    /**
     * 解析并转换输入数据。
     */
    private String resolveWorkflowUrl() {
        if (difyBaseUrl.endsWith("/")) {
            return difyBaseUrl.substring(0, difyBaseUrl.length() - 1) + WORKFLOW_RUN_PATH;
        }
        return difyBaseUrl + WORKFLOW_RUN_PATH;
    }
}
