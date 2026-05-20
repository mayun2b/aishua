package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamStartDTO {
    @NotNull(message = "试卷ID不能为空")
    @Min(value = 1, message = "试卷ID不合法")
    private Long paperId;
}
