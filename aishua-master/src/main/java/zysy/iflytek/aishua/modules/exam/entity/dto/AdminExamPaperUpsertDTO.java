package zysy.iflytek.aishua.modules.exam.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 考试数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class AdminExamPaperUpsertDTO {
    @NotBlank(message = "试卷名称不能为空")
    @Size(max = 100, message = "试卷名称长度不能超过 100")
    private String paperName;

    @NotNull(message = "学科不能为空")
    @Min(value = 1, message = "学科编号不合法")
    private Long subjectId;

    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长最少 1 分钟")
    @Max(value = 300, message = "考试时长最多 300 分钟")
    private Integer duration;

    @NotNull(message = "目标总分不能为空")
    @Min(value = 1, message = "目标总分必须大于 0")
    @Max(value = 1000, message = "目标总分不能超过 1000")
    private Integer totalScore;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    private Integer status;
}
