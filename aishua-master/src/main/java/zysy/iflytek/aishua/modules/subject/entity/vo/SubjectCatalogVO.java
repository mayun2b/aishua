package zysy.iflytek.aishua.modules.subject.entity.vo;

import lombok.Data;

@Data
public class SubjectCatalogVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private Integer questionCount;
    private Integer sort;
    private Integer isEnabled;
    private Boolean joined;
}
