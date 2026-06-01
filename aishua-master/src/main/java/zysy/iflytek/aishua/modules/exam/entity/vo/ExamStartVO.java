package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试视图对象，用于接口出参封装。
 */
@Data
public class ExamStartVO {
    private Long examRecordId;
    private Long paperId;
    private String paperName;
    private Long subjectId;
    private String subjectName;
    private Integer totalQuestions;
    private Integer totalScore;
    private Integer duration;
    private Integer status;
    private LocalDateTime startTime;
    private List<ExamQuestionItemVO> questions;
}
