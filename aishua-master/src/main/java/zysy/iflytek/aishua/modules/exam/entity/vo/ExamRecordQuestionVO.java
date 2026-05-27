package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

/**
 * 考试视图对象，负责相关业务逻辑与流程处理。
 */
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
