package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PracticeAnswerItemDTO {
    @NotNull(message = "questionId is required")
    private Long questionId;

    private String userAnswer;

    @Min(value = 0, message = "timeCost must be >= 0")
    @Max(value = 7200, message = "timeCost must be <= 7200")
    private Integer timeCost = 0;
}
