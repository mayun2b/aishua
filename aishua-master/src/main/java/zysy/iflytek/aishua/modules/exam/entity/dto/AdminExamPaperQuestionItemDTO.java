package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminExamPaperQuestionItemDTO {
    @NotNull(message = "题目ID不能为空")
    @Min(value = 1, message = "题目ID不合法")
    private Long questionId;

    @NotNull(message = "排序不能为空")
    @Min(value = 1, message = "排序必须大于 0")
    private Integer sort;

    @NotNull(message = "题目分值不能为空")
    @Min(value = 1, message = "题目分值必须大于 0")
    private Integer score;
}
