package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

/**
 * 考试视图对象，负责相关业务逻辑与流程处理。
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
