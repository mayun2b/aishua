package zysy.iflytek.aishua.modules.directory.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 目录数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class DirectoryUpsertDTO {
    @NotBlank(message = "目录名称不能为空")
    @Size(max = 100, message = "目录名称长度不能超过 100")
    private String name;

    @NotNull(message = "所属学科不能为空")
    @Min(value = 1, message = "所属学科不合法")
    private Long subjectId;

    @Min(value = 0, message = "父目录编号不能小于 0")
    private Long parentId;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值必须大于等于 0")
    private Integer sort;
}
