package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

/**
 * 考试视图对象，用于接口出参封装。
 */
@Data
public class ExamRecordQuestionVO {
    private Long id;
    private Long questionId;
    private String title;
    private Integer type;
    private Integer difficulty;
    private String imageUrls;
    private String imageDesc;
    private String options;
    private String standardAnswer;
    private String userAnswer;
    private Integer isCorrect;
    private String analysis;
    private Integer answerTime;
}
