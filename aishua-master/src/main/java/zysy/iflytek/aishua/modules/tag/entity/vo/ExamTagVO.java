package zysy.iflytek.aishua.modules.tag.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamTagVO {
    private Long id;
    private String name;
    private Long subjectId;
    private String subjectName;
    private String tag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
