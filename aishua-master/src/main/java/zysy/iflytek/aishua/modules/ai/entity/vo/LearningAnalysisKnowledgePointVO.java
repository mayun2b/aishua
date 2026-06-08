package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.Data;
import zysy.iflytek.aishua.modules.ai.entity.LearningAnalysisKnowledgePoint;

import java.math.BigDecimal;

@Data
public class LearningAnalysisKnowledgePointVO {
    private Long id;
    private String name;
    private String moduleName;
    private String reasonText;
    private String masteryLevel;
    private BigDecimal correctRate;
    private Integer sampleCount;
    private Integer wrongCount;
    private Integer priority;
    private Integer sortOrder;

    public static LearningAnalysisKnowledgePointVO from(LearningAnalysisKnowledgePoint point) {
        LearningAnalysisKnowledgePointVO vo = new LearningAnalysisKnowledgePointVO();
        vo.setId(point.getId());
        vo.setName(point.getName());
        vo.setModuleName(point.getModuleName());
        vo.setReasonText(point.getReasonText());
        vo.setMasteryLevel(point.getMasteryLevel());
        vo.setCorrectRate(point.getCorrectRate());
        vo.setSampleCount(point.getSampleCount());
        vo.setWrongCount(point.getWrongCount());
        vo.setPriority(point.getPriority());
        vo.setSortOrder(point.getSortOrder());
        return vo;
    }
}
