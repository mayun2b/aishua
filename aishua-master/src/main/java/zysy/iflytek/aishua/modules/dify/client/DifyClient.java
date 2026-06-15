package zysy.iflytek.aishua.modules.dify.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import zysy.iflytek.aishua.config.properties.DifyProperties;
import zysy.iflytek.aishua.modules.dify.entity.dto.DifyWorkflowRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify workflow client.
 */
@Component
public class DifyClient {
    private static final String INPUT_KEY_USER_AUTHORIZATION = "user_authorization";
    private static final String INPUT_KEY_QUERY = "query";
    private static final String INPUT_KEY_STUDENT_ID = "student_id";
    private static final String INPUT_KEY_SUBJECT = "subject";
    private static final String INPUT_KEY_SUBJECT_ID = "subject_id";
    private static final String INPUT_KEY_GRADE = "grade";
    private static final String INPUT_KEY_TEXTBOOK_VERSION = "textbook_version";

    private final RestTemplate restTemplate;
    private final DifyProperties difyProperties;

    public DifyClient(RestTemplate restTemplate, DifyProperties difyProperties) {
        this.restTemplate = restTemplate;
        this.difyProperties = difyProperties;
    }

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
        headers.setBearerAuth(difyProperties.getApiKey());

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
     * 通用工作流调用：自定义 inputs 和 API Key。
     */
    public Map<String, Object> runWorkflow(Map<String, Object> inputs, String userId, String apiKey) {
        DifyWorkflowRequest request = new DifyWorkflowRequest();
        request.setInputs(inputs);
        request.setResponseMode(resolveResponseMode());
        request.setUser(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

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
        request.setResponseMode(resolveResponseMode());
        request.setUser(userId);
        return request;
    }

    private String resolveWorkflowUrl() {
        String baseUrl = difyProperties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalStateException("dify.base-url is not configured");
        }
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        String workflowPath = resolveWorkflowPath();
        return normalizedBaseUrl + workflowPath;
    }

    private String resolveWorkflowPath() {
        String configured = difyProperties.getWorkflowRunPath();
        if (!StringUtils.hasText(configured)) {
            return "/v1/workflows/run";
        }
        return configured.startsWith("/") ? configured : "/" + configured;
    }

    private String resolveResponseMode() {
        String configured = difyProperties.getResponseMode();
        return StringUtils.hasText(configured) ? configured.trim() : "blocking";
    }
}
