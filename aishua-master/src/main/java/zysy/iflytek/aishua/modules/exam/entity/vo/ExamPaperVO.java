package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class ExamPaperVO {
    private Long id;
    private String paperName;
    private Long subjectId;
    private String subjectName;
    private Integer totalQuestions;
    private Integer totalScore;
    private Integer duration;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
