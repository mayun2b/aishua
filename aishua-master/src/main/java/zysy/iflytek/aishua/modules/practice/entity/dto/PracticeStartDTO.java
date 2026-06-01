package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 练习数据传输对象，用于接口入参封装。
 */
@Data
public class PracticeStartDTO {
    @NotNull(message = "学科编号不能为空")
    private Long subjectId;

    @Min(value = 1, message = "练习模式必须在 1 到 4 之间")
    @Max(value = 4, message = "练习模式必须在 1 到 4 之间")
    private Integer practiceMode = 1;

    @Min(value = 1, message = "题量必须大于等于 1")
    @Max(value = 50, message = "题量必须小于等于 50")
    private Integer questionCount = 10;

    private List<Long> tagIds;
}
