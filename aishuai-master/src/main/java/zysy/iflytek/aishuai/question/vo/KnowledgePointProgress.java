package zysy.iflytek.aishuai.question.vo;

import lombok.Data;

/**
 * 知识点进度VO
 */
@Data
public class KnowledgePointProgress {
    /**
     * 知识点ID
     */
    private Long id;
    
    /**
     * 知识点名称
     */
    private String name;
    
    /**
     * 总题目数
     */
    private Integer totalQuestions;
    
    /**
     * 已完成题目数
     */
    private Integer completedQuestions;
    
    /**
     * 进度百分比
     */
    private Integer progress;
}
