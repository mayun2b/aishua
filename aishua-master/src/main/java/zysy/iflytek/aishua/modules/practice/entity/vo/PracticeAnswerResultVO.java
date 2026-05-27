package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
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
