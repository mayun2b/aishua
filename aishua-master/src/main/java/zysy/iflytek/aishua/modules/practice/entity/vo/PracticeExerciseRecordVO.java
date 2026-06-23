package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习视图对象，用于接口出参封装。
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

    private String imageUrls;

    private String imageDesc;

    private String userAnswer;

    private String correctAnswer;

    private Integer isCorrect;

    private String aiGradingStatus;

    private Double aiGradingConfidence;

    private String aiGradingFeedback;

    private String aiGradingErrorMessage;

    private LocalDateTime aiGradedAt;

    private Integer timeCost;

    private LocalDateTime exerciseTime;
}
