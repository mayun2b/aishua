package zysy.iflytek.aishua.modules.dify.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.dify.client.DifyClient;

import java.util.Map;

@Service
public class DifyTestService {
    private static final String BEARER_PREFIX = "Bearer ";

    private final DifyClient difyClient;

    public DifyTestService(DifyClient difyClient) {
        this.difyClient = difyClient;
    }

    public Map<String, Object> testDify(String query) {
        if (!StringUtils.hasText(query)) {
            throw new BusinessException("Query cannot be empty", 400);
        }

        Long userId = UserContext.requireUserId();
        String authorization = UserContext.requireAuthorization();
        String plainToken = resolvePlainToken(authorization);
        String resolvedStudentId = String.valueOf(userId);
        return difyClient.runWorkflow(query.trim(), resolvedStudentId, String.valueOf(userId), plainToken);
    }

    /**
     * Normalize token value by stripping optional Bearer prefix.
     */
    private String resolvePlainToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            throw new BusinessException("Login token missing", 401);
        }
        String value = authorization.trim();
        return value.startsWith(BEARER_PREFIX) ? value.substring(BEARER_PREFIX.length()).trim() : value;
    }
}
