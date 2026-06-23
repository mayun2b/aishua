package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试视图对象，用于接口出参封装。
 */
@Data
public class ExamSubmitResultVO {
    private Long examRecordId;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private Double score;
    private Double objectiveScore;
    private Double subjectiveScore;
    private String gradingStatus;
    private Integer pendingSubjectiveCount;
    private Integer failedSubjectiveCount;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
