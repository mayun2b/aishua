package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PracticeAnswerItemDTO {
    @NotNull(message = "题目不能为空")
    private Long questionId;

    private String userAnswer;

    @Min(value = 0, message = "耗时不能小于 0")
    @Max(value = 7200, message = "耗时不能超过 7200 秒")
    private Integer timeCost = 0;
}
