package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 练习视图对象，用于接口出参封装。
 */
@Data
public class PracticeWrongTrendVO {
    private LocalDate statDate;

    private Integer wrongAnswerCount = 0;

    private Integer uniqueWrongQuestionCount = 0;
}
