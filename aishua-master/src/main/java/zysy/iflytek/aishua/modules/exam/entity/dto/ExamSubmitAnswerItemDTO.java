package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamSubmitAnswerItemDTO {
    @NotNull(message = "题目ID不能为空")
    @Min(value = 1, message = "题目ID不合法")
    private Long questionId;

    @Size(max = 10000, message = "答案长度不能超过 10000")
    private String userAnswer;

    @Min(value = 0, message = "答题时长不能小于 0")
    @Max(value = 36000, message = "答题时长不能超过 36000 秒")
    private Integer answerTime;
}
