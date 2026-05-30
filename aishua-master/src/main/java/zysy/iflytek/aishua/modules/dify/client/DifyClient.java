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
 * Dify 工作流客户端。
 * 仅负责将后端参数映射为 Dify API 请求并发起调用，不承载鉴权与业务规则。
 */
@Component
public class DifyClient {
    // Dify 工作流运行接口固定路径
    private static final String WORKFLOW_RUN_PATH = "/v1/workflows/run";
    // 当前采用阻塞模式，等待工作流执行完成后一次性返回
    private static final String RESPONSE_MODE_BLOCKING = "blocking";
    // Dify 工作流 inputs 中透传用户登录令牌的字段名
    private static final String INPUT_KEY_USER_AUTHORIZATION = "user_authorization";
    private static final String INPUT_KEY_QUERY = "query";
    private static final String INPUT_KEY_STUDENT_ID = "student_id";
    private static final String INPUT_KEY_SUBJECT = "subject";
    private static final String INPUT_KEY_SUBJECT_ID = "subject_id";
    private static final String INPUT_KEY_GRADE = "grade";
    private static final String INPUT_KEY_TEXTBOOK_VERSION = "textbook_version";

    private final RestTemplate restTemplate;
    private final String difyBaseUrl;
    private final String difyApiKey;

    /**
     * 注入外部调用所需依赖与配置。
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
     * 调用 Dify 工作流。
     *
     * @param query             用户输入问题
     * @param studentId         业务侧学生标识（写入 inputs.student_id）
     * @param userId            Dify 会话用户标识（写入 user）
     * @param userAuthorization 透传给工作流的用户令牌（写入 inputs.user_authorization）
     * @param subject           学科（写入 inputs.subject，可选）
     * @param subjectId         学科ID（写入 inputs.subject_id，可选）
     * @param grade             年级（写入 inputs.grade，可选）
     * @param textbookVersion   教材版本（写入 inputs.textbook_version，可选）
     * @return Dify 原始响应体；若响应体为空则返回空 Map
     */
    public Map<String, Object> runWorkflow(
            String query,
            String studentId,
            String userId,
            String userAuthorization,
            String subject,
            Long subjectId,
            String grade,
            String textbookVersion
    ) {
        DifyWorkflowRequest request = buildRequest(
                query,
                studentId,
                userId,
                userAuthorization,
                subject,
                subjectId,
                grade,
                textbookVersion
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Dify 应用级密钥，使用 Bearer 方式传递
        headers.setBearerAuth(difyApiKey);

        HttpEntity<DifyWorkflowRequest> entity = new HttpEntity<>(request, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> response = restTemplate.exchange(
                resolveWorkflowUrl(),
                HttpMethod.POST,
                entity,
                Map.class
        );

        // 兼容异常场景：上游返回 2xx 但 body 为 null
        return response.getBody() == null ? Map.of() : response.getBody();
    }

    /**
     * 组装 Dify 请求体。
     * 仅在可选字段有值时放入 inputs，避免覆盖工作流侧默认值。
     */
    private DifyWorkflowRequest buildRequest(
            String query,
            String studentId,
            String userId,
            String userAuthorization,
            String subject,
            Long subjectId,
            String grade,
            String textbookVersion
    ) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(INPUT_KEY_QUERY, query);
        if (StringUtils.hasText(studentId)) {
            inputs.put(INPUT_KEY_STUDENT_ID, studentId.trim());
        }
        if (StringUtils.hasText(userAuthorization)) {
            inputs.put(INPUT_KEY_USER_AUTHORIZATION, userAuthorization.trim());
        }
        if (StringUtils.hasText(subject)) {
            inputs.put(INPUT_KEY_SUBJECT, subject.trim());
        }
        if (subjectId != null && subjectId > 0) {
            inputs.put(INPUT_KEY_SUBJECT_ID, subjectId);
        }
        if (StringUtils.hasText(grade)) {
            inputs.put(INPUT_KEY_GRADE, grade.trim());
        }
        if (StringUtils.hasText(textbookVersion)) {
            inputs.put(INPUT_KEY_TEXTBOOK_VERSION, textbookVersion.trim());
        }

        DifyWorkflowRequest request = new DifyWorkflowRequest();
        request.setInputs(inputs);
        request.setResponseMode(RESPONSE_MODE_BLOCKING);
        request.setUser(userId);
        return request;
    }

    /**
     * 拼接工作流调用地址，兼容 base-url 尾部是否包含 '/' 两种配置。
     */
    private String resolveWorkflowUrl() {
        if (difyBaseUrl.endsWith("/")) {
            return difyBaseUrl.substring(0, difyBaseUrl.length() - 1) + WORKFLOW_RUN_PATH;
        }
        return difyBaseUrl + WORKFLOW_RUN_PATH;
    }
}
