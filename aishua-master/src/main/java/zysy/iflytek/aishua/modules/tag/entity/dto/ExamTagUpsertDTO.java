package zysy.iflytek.aishua.modules.tag.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExamTagUpsertDTO {
    @NotBlank(message = "考点名称不能为空")
    @Size(max = 100, message = "考点名称长度不能超过 100")
    private String name;

    @NotNull(message = "所属学科不能为空")
    @Min(value = 1, message = "所属学科不合法")
    private Long subjectId;

    @Size(max = 255, message = "备注长度不能超过 255")
    private String tag;
}
