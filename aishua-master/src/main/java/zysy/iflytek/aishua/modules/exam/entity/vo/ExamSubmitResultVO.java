package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class ExamSubmitResultVO {
    private Long examRecordId;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private Double score;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
