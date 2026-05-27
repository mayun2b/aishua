package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeQuestionItemVO {
    private Long questionId;

    private String title;

    private String content;

    private Integer type;

    private String options;

    private String imageUrls;

    private String imageDesc;

    private Integer difficulty;
}
