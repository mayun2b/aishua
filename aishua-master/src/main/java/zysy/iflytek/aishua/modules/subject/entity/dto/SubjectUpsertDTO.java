package zysy.iflytek.aishua.modules.subject.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubjectUpsertDTO {
    @NotBlank(message = "学科名称不能为空")
    @Size(max = 100, message = "学科名称长度不能超过 100")
    private String name;

    @NotBlank(message = "学科编码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{2,50}$", message = "学科编码长度需在 2-50 且仅允许字母、数字、下划线、短横线")
    private String code;

    @Size(max = 1000, message = "学科描述长度不能超过 1000")
    private String description;

    @Size(max = 255, message = "图标地址长度不能超过 255")
    private String icon;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值必须大于等于 0")
    private Integer sort;

    @NotNull(message = "启用状态不能为空")
    @Min(value = 0, message = "启用状态只能是 0 或 1")
    @Max(value = 1, message = "启用状态只能是 0 或 1")
    private Integer isEnabled;
}
