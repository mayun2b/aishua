package zysy.iflytek.aishua.modules.dify.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.ai.entity.vo.LearningAnalysisReportVO;
import zysy.iflytek.aishua.modules.ai.service.LearningAnalysisReportService;
import zysy.iflytek.aishua.modules.dify.client.DifyClient;
import zysy.iflytek.aishua.modules.dify.entity.dto.DifyTestRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dify 联调服务。
 * 负责从用户上下文提取必要信息并调用 DifyClient。
 */
@Service
public class DifyTestService {
    // 前端请求头常见前缀，透传给工作流前会去掉该前缀
    private static final String BEARER_PREFIX = "Bearer ";

    private final DifyClient difyClient;
    private final LearningAnalysisReportService learningAnalysisReportService;

    public DifyTestService(DifyClient difyClient, LearningAnalysisReportService learningAnalysisReportService) {
        this.difyClient = difyClient;
        this.learningAnalysisReportService = learningAnalysisReportService;
    }

    /**
     * 调用 Dify 测试工作流。
     *
     * 调用链路：
     * 1. 入参基础校验由 Controller 层 @Valid 完成
     * 2. 从 UserContext 获取当前登录用户与令牌
     * 3. 组装 studentId / userId 与扩展业务字段并调用 DifyClient
     */
    public Map<String, Object> testDify(DifyTestRequest request) {
        // 鉴权拦截器已将 userId 与 token 写入 UserContext
        Long userId = UserContext.requireUserId();
        String authorization = UserContext.requireAuthorization();
        String plainToken = resolvePlainToken(authorization);

        String resolvedUserId = String.valueOf(userId);
        // 未显式传 studentId 时，默认使用当前登录用户 ID
        String resolvedStudentId = StringUtils.hasText(request.getStudentId())
                ? request.getStudentId().trim()
                : resolvedUserId;

        Map<String, Object> difyResponse = difyClient.runWorkflow(
                request.getQuery().trim(),
                resolvedStudentId,
                resolvedUserId,
                plainToken,
                normalizeNullableText(request.getSubject()),
                normalizeNullableSubjectId(request.getSubjectId()),
                normalizeNullableText(request.getGrade()),
                normalizeNullableText(request.getTextbookVersion())
        );

        LearningAnalysisReportVO analysisReport = learningAnalysisReportService.saveDifyResult(userId, request, difyResponse);
        Map<String, Object> result = new LinkedHashMap<>(difyResponse);
        result.put("analysisReport", analysisReport);
        return result;
    }

    /**
     * 规范化令牌格式：兼容 "Bearer xxx" 与 "xxx" 两种输入。
     */
    private String resolvePlainToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            throw new BusinessException("Login token missing", 401);
        }
        String value = authorization.trim();
        return value.startsWith(BEARER_PREFIX) ? value.substring(BEARER_PREFIX.length()).trim() : value;
    }

    /**
     * 将可选文本字段规范化为“去空白后的值或 null”。
     */
    private String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 学科编号仅允许正整数；空值代表不限制。
     */
    private Long normalizeNullableSubjectId(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        if (subjectId <= 0) {
            throw new BusinessException("subjectId 不合法", 400);
        }
        return subjectId;
    }
}
