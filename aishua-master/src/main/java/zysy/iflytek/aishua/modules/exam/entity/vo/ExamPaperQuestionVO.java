package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

/**
 * 考试视图对象，用于接口出参封装。
 */
@Data
public class ExamPaperQuestionVO {
    private Long questionId;
    private String title;
    private Integer type;
    private Integer difficulty;
    private String options;
    private String answer;
    private String analysis;
    private Integer sort;
    private Integer score;
}
