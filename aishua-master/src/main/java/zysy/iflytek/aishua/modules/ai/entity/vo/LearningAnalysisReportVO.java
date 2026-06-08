package zysy.iflytek.aishua.modules.ai.entity.vo;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import zysy.iflytek.aishua.modules.ai.entity.LearningAnalysisKnowledgePoint;
import zysy.iflytek.aishua.modules.ai.entity.LearningAnalysisReport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class LearningAnalysisReportVO {
    private Long id;
    private String reportCode;
    private Long userId;
    private Long subjectId;
    private String subjectName;
    private String grade;
    private String textbookVersion;
    private String queryText;
    private String summary;
    private String fullText;
    private Object result;
    private Object rawResponse;
    private Integer dataQualitySufficient;
    private Object missingMetrics;
    private String workflowRunId;
    private String taskId;
    private Integer status;
    private String errorMessage;
    private LocalDateTime createTime;
    private List<LearningAnalysisKnowledgePointVO> knowledgePoints = new ArrayList<>();

    public static LearningAnalysisReportVO summaryFrom(LearningAnalysisReport report) {
        LearningAnalysisReportVO vo = new LearningAnalysisReportVO();
        vo.setId(report.getId());
        vo.setReportCode(report.getReportCode());
        vo.setUserId(report.getUserId());
        vo.setSubjectId(report.getSubjectId());
        vo.setSubjectName(report.getSubjectName());
        vo.setGrade(report.getGrade());
        vo.setTextbookVersion(report.getTextbookVersion());
        vo.setQueryText(report.getQueryText());
        vo.setSummary(report.getSummary());
        vo.setDataQualitySufficient(report.getDataQualitySufficient());
        vo.setWorkflowRunId(report.getWorkflowRunId());
        vo.setTaskId(report.getTaskId());
        vo.setStatus(report.getStatus());
        vo.setErrorMessage(report.getErrorMessage());
        vo.setCreateTime(report.getCreateTime());
        return vo;
    }

    public static LearningAnalysisReportVO from(LearningAnalysisReport report, List<LearningAnalysisKnowledgePoint> points) {
        LearningAnalysisReportVO vo = new LearningAnalysisReportVO();
        vo.setId(report.getId());
        vo.setReportCode(report.getReportCode());
        vo.setUserId(report.getUserId());
        vo.setSubjectId(report.getSubjectId());
        vo.setSubjectName(report.getSubjectName());
        vo.setGrade(report.getGrade());
        vo.setTextbookVersion(report.getTextbookVersion());
        vo.setQueryText(report.getQueryText());
        vo.setSummary(report.getSummary());
        vo.setFullText(report.getFullText());
        vo.setResult(parseJson(report.getResultJson()));
        vo.setRawResponse(parseJson(report.getRawResponseJson()));
        vo.setDataQualitySufficient(report.getDataQualitySufficient());
        vo.setMissingMetrics(parseJson(report.getMissingMetrics()));
        vo.setWorkflowRunId(report.getWorkflowRunId());
        vo.setTaskId(report.getTaskId());
        vo.setStatus(report.getStatus());
        vo.setErrorMessage(report.getErrorMessage());
        vo.setCreateTime(report.getCreateTime());
        vo.setKnowledgePoints(points.stream().map(LearningAnalysisKnowledgePointVO::from).toList());
        return vo;
    }

    private static Object parseJson(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return JSON.parse(text);
        } catch (Exception ignored) {
            return text;
        }
    }
}
