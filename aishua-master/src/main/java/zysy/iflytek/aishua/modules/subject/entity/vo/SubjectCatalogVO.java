package zysy.iflytek.aishua.modules.subject.entity.vo;

import lombok.Data;

/**
 * 学科视图对象，用于接口出参封装。
 */
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
