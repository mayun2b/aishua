package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeDraftAnswerVO {
    private Long questionId;

    private String userAnswer;

    private Integer timeCost;

    private Long updatedAt;
}
