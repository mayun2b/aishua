package zysy.iflytek.aishua.modules.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.modules.ai.entity.LearningAnalysisKnowledgePoint;
import zysy.iflytek.aishua.modules.ai.entity.LearningAnalysisReport;
import zysy.iflytek.aishua.modules.ai.entity.vo.LearningAnalysisReportVO;
import zysy.iflytek.aishua.modules.ai.mapper.LearningAnalysisKnowledgePointMapper;
import zysy.iflytek.aishua.modules.ai.mapper.LearningAnalysisReportMapper;
import zysy.iflytek.aishua.modules.ai.service.LearningAnalysisReportService;
import zysy.iflytek.aishua.modules.dify.entity.dto.DifyTestRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class LearningAnalysisReportServiceImpl implements LearningAnalysisReportService {
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;

    private final LearningAnalysisReportMapper reportMapper;
    private final LearningAnalysisKnowledgePointMapper knowledgePointMapper;

    public LearningAnalysisReportServiceImpl(
            LearningAnalysisReportMapper reportMapper,
            LearningAnalysisKnowledgePointMapper knowledgePointMapper
    ) {
        this.reportMapper = reportMapper;
        this.knowledgePointMapper = knowledgePointMapper;
    }

    @Override
    @Transactional
    public LearningAnalysisReportVO saveDifyResult(Long userId, DifyTestRequest request, Map<String, Object> difyResponse) {
        if (userId == null) {
            throw new BusinessException("用户上下文缺失", 401);
        }
        JSONObject rawResponse = toObject(difyResponse);
        JSONObject data = firstObject(rawResponse, "data");
        JSONObject outputs = resolveOutputs(rawResponse, data);
        JSONObject structuredOutput = resolveStructuredOutput(outputs, rawResponse, data);
        JSONObject dataQuality = firstObject(structuredOutput, "dataQuality", "data_quality");
        LocalDateTime now = LocalDateTime.now();

        LearningAnalysisReport report = new LearningAnalysisReport();
        report.setReportCode(newReportCode());
        report.setUserId(userId);
        report.setSubjectId(request.getSubjectId());
        report.setSubjectName(normalizeText(firstNonBlank(request.getSubject(), firstString(structuredOutput, "subject"))));
        report.setGrade(normalizeText(firstNonBlank(request.getGrade(), firstString(structuredOutput, "grade"))));
        report.setTextbookVersion(normalizeText(request.getTextbookVersion()));
        report.setQueryText(request.getQuery() == null ? "" : request.getQuery().trim());
        report.setSummary(resolveSummary(outputs, structuredOutput));
        report.setFullText(resolveFullText(outputs, structuredOutput));
        report.setResultJson(resolveResultJson(outputs, structuredOutput));
        report.setRawResponseJson(JSON.toJSONString(rawResponse));
        report.setDataQualitySufficient(booleanToTinyInt(firstBoolean(dataQuality, "sufficient")));
        report.setMissingMetrics(arrayToJson(firstArray(dataQuality, "missingMetrics", "missing_metrics")));
        report.setWorkflowRunId(firstNonBlank(
                firstString(rawResponse, "workflow_run_id", "workflowRunId"),
                firstString(data, "id", "workflow_run_id", "workflowRunId")
        ));
        report.setTaskId(firstString(rawResponse, "task_id", "taskId"));
        report.setErrorMessage(firstNonBlank(firstString(data, "error"), firstString(rawResponse, "error")));
        report.setStatus(StringUtils.hasText(report.getErrorMessage()) ? STATUS_FAILED : STATUS_SUCCESS);
        report.setCreateTime(now);
        report.setUpdateTime(now);

        LearningAnalysisReport existing = selectByWorkflowRunId(report.getWorkflowRunId());
        if (existing != null) {
            report.setId(existing.getId());
            report.setReportCode(existing.getReportCode());
            report.setCreateTime(existing.getCreateTime());
            reportMapper.updateById(report);
            knowledgePointMapper.delete(new LambdaQueryWrapper<LearningAnalysisKnowledgePoint>()
                    .eq(LearningAnalysisKnowledgePoint::getReportId, existing.getId()));
        } else {
            reportMapper.insert(report);
        }

        List<LearningAnalysisKnowledgePoint> points = parseKnowledgePoints(report.getId(), outputs, structuredOutput, now);
        for (LearningAnalysisKnowledgePoint point : points) {
            knowledgePointMapper.insert(point);
        }
        return LearningAnalysisReportVO.from(report, points);
    }

    @Override
    public LearningAnalysisReportVO getById(Long userId, Long reportId) {
        LearningAnalysisReport report = reportMapper.selectOne(new LambdaQueryWrapper<LearningAnalysisReport>()
                .eq(LearningAnalysisReport::getId, reportId)
                .eq(LearningAnalysisReport::getUserId, userId)
                .last("limit 1"));
        if (report == null) {
            throw new BusinessException("学情分析报告不存在", 404);
        }
        return LearningAnalysisReportVO.from(report, selectPoints(report.getId()));
    }

    @Override
    public LearningAnalysisReportVO getLatest(Long userId, Long subjectId) {
        validateSubjectFilter(subjectId);
        LambdaQueryWrapper<LearningAnalysisReport> wrapper = new LambdaQueryWrapper<LearningAnalysisReport>()
                .eq(LearningAnalysisReport::getUserId, userId)
                .orderByDesc(LearningAnalysisReport::getCreateTime)
                .orderByDesc(LearningAnalysisReport::getId)
                .last("limit 1");
        if (subjectId != null && subjectId > 0) {
            wrapper.eq(LearningAnalysisReport::getSubjectId, subjectId);
        }
        LearningAnalysisReport report = reportMapper.selectOne(wrapper);
        if (report == null) {
            throw new BusinessException("暂无学情分析报告", 404);
        }
        return LearningAnalysisReportVO.from(report, selectPoints(report.getId()));
    }

    @Override
    public PageResult<LearningAnalysisReportVO> listHistory(Long userId, Long subjectId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException("用户上下文缺失", 401);
        }
        validateSubjectFilter(subjectId);

        int safePageNum = PageResult.normalizePageNum(pageNum);
        int safePageSize = PageResult.normalizePageSize(pageSize);
        Long total = reportMapper.selectCount(new LambdaQueryWrapper<LearningAnalysisReport>()
                .eq(LearningAnalysisReport::getUserId, userId)
                .eq(subjectId != null, LearningAnalysisReport::getSubjectId, subjectId));
        if (total == null || total <= 0) {
            return PageResult.empty(safePageNum, safePageSize);
        }

        List<LearningAnalysisReport> reports = reportMapper.selectList(new LambdaQueryWrapper<LearningAnalysisReport>()
                .eq(LearningAnalysisReport::getUserId, userId)
                .eq(subjectId != null, LearningAnalysisReport::getSubjectId, subjectId)
                .orderByDesc(LearningAnalysisReport::getCreateTime)
                .orderByDesc(LearningAnalysisReport::getId)
                .last("LIMIT " + PageResult.offset(safePageNum, safePageSize) + ", " + safePageSize));
        if (reports.isEmpty()) {
            return PageResult.of(Collections.emptyList(), total, safePageNum, safePageSize);
        }

        List<LearningAnalysisReportVO> records = reports.stream()
                .map(LearningAnalysisReportVO::summaryFrom)
                .toList();
        return PageResult.of(records, total, safePageNum, safePageSize);
    }

    private JSONObject resolveOutputs(JSONObject rawResponse, JSONObject data) {
        JSONObject outputs = firstObject(data, "outputs");
        if (!outputs.isEmpty()) {
            return outputs;
        }
        return firstObject(rawResponse, "outputs");
    }

    private JSONObject resolveStructuredOutput(JSONObject outputs, JSONObject rawResponse, JSONObject data) {
        JSONObject structuredOutput = firstObject(outputs, "structured_output", "structuredOutput");
        if (!structuredOutput.isEmpty()) {
            return structuredOutput;
        }
        structuredOutput = firstObject(data, "structured_output", "structuredOutput");
        if (!structuredOutput.isEmpty()) {
            return structuredOutput;
        }
        structuredOutput = firstObject(rawResponse, "structured_output", "structuredOutput");
        if (!structuredOutput.isEmpty()) {
            return structuredOutput;
        }
        if (hasAny(outputs, "summary", "weakKnowledgePoints", "weak_knowledge_points", "errorReasons", "suggestions", "nextFocus")) {
            return outputs;
        }
        return new JSONObject();
    }

    private String resolveSummary(JSONObject outputs, JSONObject structuredOutput) {
        return firstNonBlank(
                firstString(structuredOutput, "summary", "summaryText"),
                firstString(outputs, "summary", "analysis", "answer", "result")
        );
    }

    private String resolveFullText(JSONObject outputs, JSONObject structuredOutput) {
        return firstNonBlank(
                firstString(outputs, "text", "fullText"),
                firstString(structuredOutput, "fullText", "text")
        );
    }

    private String resolveResultJson(JSONObject outputs, JSONObject structuredOutput) {
        if (!structuredOutput.isEmpty()) {
            return JSON.toJSONString(structuredOutput);
        }
        return outputs.isEmpty() ? null : JSON.toJSONString(outputs);
    }

    private LearningAnalysisReport selectByWorkflowRunId(String workflowRunId) {
        if (!StringUtils.hasText(workflowRunId)) {
            return null;
        }
        return reportMapper.selectOne(new LambdaQueryWrapper<LearningAnalysisReport>()
                .eq(LearningAnalysisReport::getWorkflowRunId, workflowRunId)
                .last("limit 1"));
    }

    private void validateSubjectFilter(Long subjectId) {
        if (subjectId != null && subjectId <= 0) {
            throw new BusinessException("学科编号不合法", 400);
        }
    }

    private List<LearningAnalysisKnowledgePoint> selectPoints(Long reportId) {
        return knowledgePointMapper.selectList(new LambdaQueryWrapper<LearningAnalysisKnowledgePoint>()
                .eq(LearningAnalysisKnowledgePoint::getReportId, reportId)
                .orderByAsc(LearningAnalysisKnowledgePoint::getSortOrder)
                .orderByAsc(LearningAnalysisKnowledgePoint::getId));
    }

    private List<LearningAnalysisKnowledgePoint> parseKnowledgePoints(Long reportId, JSONObject outputs, JSONObject structuredOutput, LocalDateTime now) {
        JSONArray structuredPoints = firstArray(structuredOutput, "weakKnowledgePoints", "weak_knowledge_points");
        if (structuredPoints != null && !structuredPoints.isEmpty()) {
            return parsePointArray(reportId, structuredPoints, now);
        }
        JSONArray legacyArray = firstArray(outputs, "knowledge_points", "knowledgePoints");
        if (legacyArray != null && !legacyArray.isEmpty()) {
            return parsePointArray(reportId, legacyArray, now);
        }
        String legacyNames = firstString(outputs, "knowledge_points", "knowledgePoints");
        return parsePointNames(reportId, legacyNames, now);
    }

    private List<LearningAnalysisKnowledgePoint> parsePointArray(Long reportId, JSONArray pointsArray, LocalDateTime now) {
        List<LearningAnalysisKnowledgePoint> points = new ArrayList<>();
        int sortOrder = 1;
        for (Object item : pointsArray) {
            LearningAnalysisKnowledgePoint point = new LearningAnalysisKnowledgePoint();
            point.setReportId(reportId);
            point.setSortOrder(sortOrder);
            point.setCreateTime(now);
            point.setUpdateTime(now);
            if (item instanceof String) {
                point.setName(((String) item).trim());
            } else {
                JSONObject object = toObject(item);
                point.setName(firstString(object, "name", "knowledgePoint", "knowledge_point"));
                point.setModuleName(firstString(object, "module", "moduleName", "module_name"));
                point.setReasonText(firstString(object, "reason", "reasonText", "reason_text"));
                point.setMasteryLevel(firstString(object, "masteryLevel", "mastery_level"));
                point.setCorrectRate(firstBigDecimal(object, "correctRate", "correct_rate", "accuracy"));
                point.setSampleCount(firstInteger(object, "sampleCount", "sample_count", "totalCount", "total_count"));
                point.setWrongCount(firstInteger(object, "wrongCount", "wrong_count"));
                point.setPriority(firstInteger(object, "priority"));
            }
            if (StringUtils.hasText(point.getName())) {
                points.add(point);
                sortOrder++;
            }
        }
        return points;
    }

    private List<LearningAnalysisKnowledgePoint> parsePointNames(Long reportId, String namesText, LocalDateTime now) {
        List<LearningAnalysisKnowledgePoint> points = new ArrayList<>();
        if (!StringUtils.hasText(namesText)) {
            return points;
        }
        String[] names = namesText.split("[,，、;；\\n\\r]+");
        int sortOrder = 1;
        for (String name : names) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            LearningAnalysisKnowledgePoint point = new LearningAnalysisKnowledgePoint();
            point.setReportId(reportId);
            point.setName(name.trim());
            point.setSortOrder(sortOrder++);
            point.setCreateTime(now);
            point.setUpdateTime(now);
            points.add(point);
        }
        return points;
    }

    private String newReportCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private JSONObject toObject(Object value) {
        if (value == null) {
            return new JSONObject();
        }
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        try {
            if (value instanceof String) {
                String text = ((String) value).trim();
                return text.isEmpty() ? new JSONObject() : JSON.parseObject(text);
            }
            if (value instanceof Map) {
                return JSON.parseObject(JSON.toJSONString(value));
            }
            return JSON.parseObject(JSON.toJSONString(value));
        } catch (Exception e) {
            log.warn("Cannot parse object value: {}", value, e);
            return new JSONObject();
        }
    }

    private JSONObject firstObject(JSONObject object, String... keys) {
        return toObject(firstValue(object, keys));
    }

    private JSONArray firstArray(JSONObject object, String... keys) {
        Object value = firstValue(object, keys);
        if (value == null) {
            return null;
        }
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        try {
            if (value instanceof String) {
                String text = ((String) value).trim();
                if (text.isEmpty()) {
                    return null;
                }
                if (!text.startsWith("[")) {
                    return null;
                }
                return JSON.parseArray(text);
            }
            return JSON.parseArray(JSON.toJSONString(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String firstString(JSONObject object, String... keys) {
        Object value = firstValue(object, keys);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Integer firstInteger(JSONObject object, String... keys) {
        Object value = firstValue(object, keys);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal firstBigDecimal(JSONObject object, String... keys) {
        Object value = firstValue(object, keys);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean firstBoolean(JSONObject object, String... keys) {
        Object value = firstValue(object, keys);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String text = String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        return null;
    }

    private Object firstValue(JSONObject object, String... keys) {
        if (object == null || object.isEmpty()) {
            return null;
        }
        for (String key : keys) {
            if (object.containsKey(key) && object.get(key) != null) {
                return object.get(key);
            }
        }
        return null;
    }

    private boolean hasAny(JSONObject object, String... keys) {
        if (object == null || object.isEmpty()) {
            return false;
        }
        for (String key : keys) {
            if (object.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        return null;
    }

    private String normalizeText(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private Integer booleanToTinyInt(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? 1 : 0;
    }

    private String arrayToJson(JSONArray array) {
        return array == null ? null : JSON.toJSONString(array);
    }
}
