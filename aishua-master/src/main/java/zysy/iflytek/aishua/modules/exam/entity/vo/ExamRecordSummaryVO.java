package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRecordSummaryVO {
    private Long id;
    private Long userId;
    private String userNickname;
    private String userPhone;
    private Long subjectId;
    private String subjectName;
    private String examName;
    private Integer examMode;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private Double score;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
