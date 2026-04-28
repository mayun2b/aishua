package zysy.iflytek.aishua.modules.subject.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MySubjectVO {
    private Long userSubjectId;
    private Long subjectId;
    private String name;
    private String code;
    private String description;
    private String icon;
    private Integer questionCount;
    private Integer sort;
    private Integer isEnabled;
    private Integer status;
    private LocalDateTime joinedAt;
    private LocalDateTime lastPracticeAt;
}
