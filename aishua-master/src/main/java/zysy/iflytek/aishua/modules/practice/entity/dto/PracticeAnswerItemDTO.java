package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 练习数据传输对象，用于接口入参封装。
 */
@Data
public class PracticeAnswerItemDTO {
    @NotNull(message = "题目编号不能为空")
    private Long questionId;

    private String userAnswer;

    @Min(value = 0, message = "作答耗时必须大于等于 0")
    @Max(value = 7200, message = "作答耗时必须小于等于 7200")
    private Integer timeCost = 0;
}
