package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 智能问答视图对象，用于接口出参封装。
 */
@Data
public class WrongQuestionAiAnalysisVO {
    private Long analysisId;

    private String analysisCode;

    private Long wrongQuestionId;

    private Long questionId;

    private String summary;

    private List<String> errorReasons = new ArrayList<>();

    private List<String> reasonEvidence = new ArrayList<>();

    private List<String> solutionSteps = new ArrayList<>();

    private List<String> knowledgePoints = new ArrayList<>();

    private List<String> avoidanceTips = new ArrayList<>();

    private List<String> followUpQuestions = new ArrayList<>();

    private Integer status;

    private String errorMessage;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer latencyMs;

    private LocalDateTime createTime;
}
