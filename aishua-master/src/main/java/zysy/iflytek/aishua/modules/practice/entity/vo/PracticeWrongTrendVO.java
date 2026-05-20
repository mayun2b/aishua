package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PracticeWrongTrendVO {
    private LocalDate statDate;

    private Integer wrongAnswerCount = 0;

    private Integer uniqueWrongQuestionCount = 0;
}
