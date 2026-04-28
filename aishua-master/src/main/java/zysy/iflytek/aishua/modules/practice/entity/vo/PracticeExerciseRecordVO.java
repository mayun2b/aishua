package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

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
