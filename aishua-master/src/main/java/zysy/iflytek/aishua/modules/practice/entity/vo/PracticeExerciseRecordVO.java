package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeExerciseRecordVO {
    private Long recordId;

    private Long sessionId;

    private Integer practiceMode;

    private Long subjectId;

    private String subjectName;

    private Long questionId;

    private String questionTitle;

    private Integer questionType;

    private Integer difficulty;

    private String userAnswer;

    private String correctAnswer;

    private Integer isCorrect;

    private Integer timeCost;

    private LocalDateTime exerciseTime;
}
