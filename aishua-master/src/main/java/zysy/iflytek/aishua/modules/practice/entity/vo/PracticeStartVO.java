package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PracticeStartVO {
    private Long sessionId;

    private String sessionCode;

    private Long subjectId;

    private String subjectName;

    private Integer practiceMode;

    private Integer questionCount;

    private Integer answeredCount;

    private Integer correctCount;

    private Integer wrongCount;

    private Integer status;

    private LocalDateTime startedAt;
}
