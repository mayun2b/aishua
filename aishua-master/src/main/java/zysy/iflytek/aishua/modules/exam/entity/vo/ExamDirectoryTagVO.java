package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

/**
 * 考试视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class ExamDirectoryTagVO {
    private Long tagId;
    private String tagName;
    private Integer relationType;
    private Integer importanceLevel;
    private Integer examFrequency;
    private Integer sort;
    private Integer questionCount;
}

