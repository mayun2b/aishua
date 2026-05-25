package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

@Data
public class PracticeDraftAnswerVO {
    private Long questionId;

    private String userAnswer;

    private Integer timeCost;

    private Long updatedAt;
}
