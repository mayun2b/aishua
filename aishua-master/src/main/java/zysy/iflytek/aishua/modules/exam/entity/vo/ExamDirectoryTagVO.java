package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

/**
 * 考试视图对象，用于接口出参封装。
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

