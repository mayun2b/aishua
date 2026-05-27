package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeWrongTrendVO {
    private LocalDate statDate;

    private Integer wrongAnswerCount = 0;

    private Integer uniqueWrongQuestionCount = 0;
}
