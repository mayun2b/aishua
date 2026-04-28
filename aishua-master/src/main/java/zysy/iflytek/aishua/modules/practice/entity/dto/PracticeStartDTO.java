package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PracticeStartDTO {
    @NotNull(message = "学科不能为空")
    private Long subjectId;

    @Min(value = 1, message = "练习模式不合法")
    @Max(value = 4, message = "练习模式不合法")
    private Integer practiceMode = 1;

    @Min(value = 1, message = "题目数量至少为 1")
    @Max(value = 50, message = "题目数量不能超过 50")
    private Integer questionCount = 10;
}
