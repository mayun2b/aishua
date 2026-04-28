package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

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
