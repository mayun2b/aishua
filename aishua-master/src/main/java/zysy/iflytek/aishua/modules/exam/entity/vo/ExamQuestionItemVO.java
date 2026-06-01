package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

/**
 * 考试视图对象，用于接口出参封装。
 */
@Data
public class ExamQuestionItemVO {
    private Long questionId;
    private String title;
    private String content;
    private Integer type;
    private String options;
    private String imageUrls;
    private String imageDesc;
    private Integer difficulty;
    private Integer sort;
    private Integer score;
}
