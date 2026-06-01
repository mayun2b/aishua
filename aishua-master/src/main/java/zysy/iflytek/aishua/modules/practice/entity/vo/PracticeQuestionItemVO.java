package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

/**
 * 练习视图对象，用于接口出参封装。
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
