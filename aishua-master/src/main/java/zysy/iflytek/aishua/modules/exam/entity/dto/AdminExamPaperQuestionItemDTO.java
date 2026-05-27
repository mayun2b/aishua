package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 考试数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class AdminExamPaperQuestionItemDTO {
    @NotNull(message = "题目编号不能为空")
    @Min(value = 1, message = "题目编号不合法")
    private Long questionId;

    @NotNull(message = "排序不能为空")
    @Min(value = 1, message = "排序必须大于 0")
    private Integer sort;

    @NotNull(message = "题目分值不能为空")
    @Min(value = 1, message = "题目分值必须大于 0")
    private Integer score;
}
