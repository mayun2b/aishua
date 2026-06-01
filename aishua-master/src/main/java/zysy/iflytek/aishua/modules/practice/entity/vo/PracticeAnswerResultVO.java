package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

/**
 * 练习视图对象，用于接口出参封装。
 */
@Data
public class PracticeAnswerResultVO {
    private Long questionId;

    private String questionTitle;

    private String userAnswer;

    private String correctAnswer;

    private String analysis;

    private Integer isCorrect;

    private Integer timeCost;
}
