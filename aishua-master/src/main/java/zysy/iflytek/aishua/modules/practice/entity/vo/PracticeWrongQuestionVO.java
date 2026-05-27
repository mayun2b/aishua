package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeWrongQuestionVO {
    private Long wrongQuestionId;

    private Long subjectId;

    private String subjectName;

    private Long directoryId;

    private String directoryName;

    private Long questionId;

    private String questionTitle;

    private Integer questionType;

    private Integer difficulty;

    private String correctAnswer;

    private String analysis;

    private String tags;

    private Integer wrongCount;

    private Integer masterStatus;

    private LocalDateTime lastWrongTime;
}
