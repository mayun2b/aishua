package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 考试数据传输对象，用于接口入参封装。
 */
@Data
public class ExamStartDTO {
    @NotNull(message = "试卷编号不能为空")
    @Min(value = 1, message = "试卷编号不合法")
    private Long paperId;
}
