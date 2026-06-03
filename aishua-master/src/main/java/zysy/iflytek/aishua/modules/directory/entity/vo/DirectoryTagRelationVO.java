package zysy.iflytek.aishua.modules.directory.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 目录-考点关系出参对象。
 */
@Data
public class DirectoryTagRelationVO {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Long directoryId;
    private String directoryName;
    private Long tagId;
    private String tagName;
    private String tagRemark;
    private Integer relationType;
    private Integer importanceLevel;
    private Integer examFrequency;
    private Integer sort;
    private Integer isEnabled;
    private Integer sourceType;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
