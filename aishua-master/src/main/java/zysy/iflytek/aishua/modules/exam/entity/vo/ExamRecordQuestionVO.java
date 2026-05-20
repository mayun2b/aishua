package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

@Data
public class ExamRecordQuestionVO {
    private Long id;
    private Long questionId;
    private String title;
    private Integer type;
    private Integer difficulty;
    private String options;
    private String standardAnswer;
    private String userAnswer;
    private Integer isCorrect;
    private String analysis;
    private Integer answerTime;
}
