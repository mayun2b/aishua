package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

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

