package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
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
