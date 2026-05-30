package zysy.iflytek.aishua.modules.subject.entity.vo;

import lombok.Data;

/**
 * 用户端目录考点视图对象。
 */
@Data
public class SubjectDirectoryTagVO {
    private Long tagId;
    private String tagName;
    private Integer relationType;
    private Integer importanceLevel;
    private Integer examFrequency;
    private Integer sort;
    private Integer questionCount;
}
