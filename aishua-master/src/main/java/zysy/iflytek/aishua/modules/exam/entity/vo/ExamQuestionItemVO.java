package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

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
