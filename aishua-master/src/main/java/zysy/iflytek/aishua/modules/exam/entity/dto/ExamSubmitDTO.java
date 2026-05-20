package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitDTO {
    @NotEmpty(message = "提交答案不能为空")
    @Valid
    private List<ExamSubmitAnswerItemDTO> answers;

    @Min(value = 0, message = "考试用时不能小于 0")
    private Integer duration;
}
