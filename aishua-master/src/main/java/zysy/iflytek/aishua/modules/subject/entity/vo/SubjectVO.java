package zysy.iflytek.aishua.modules.subject.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubjectVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private Integer questionCount;
    private Integer sort;
    private Integer isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
