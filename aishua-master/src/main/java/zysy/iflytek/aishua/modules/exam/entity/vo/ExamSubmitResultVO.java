package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

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
