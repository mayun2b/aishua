package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

/**
 * 练习视图对象，用于接口出参封装。
 */
@Data
public class PracticeDraftAnswerVO {
    private Long questionId;

    private String userAnswer;

    private Integer timeCost;

    private Long updatedAt;
}
