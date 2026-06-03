package zysy.iflytek.aishua.modules.directory.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 目录-考点关系入参对象。
 */
@Data
public class DirectoryTagRelationUpsertDTO {
    @NotNull(message = "学科编号不能为空")
    @Min(value = 1, message = "学科编号不合法")
    private Long subjectId;

    @NotNull(message = "目录编号不能为空")
    @Min(value = 1, message = "目录编号不合法")
    private Long directoryId;

    @NotNull(message = "考点编号不能为空")
    @Min(value = 1, message = "考点编号不合法")
    private Long tagId;

    @Min(value = 1, message = "关系类型不合法")
    @Max(value = 3, message = "关系类型不合法")
    private Integer relationType = 1;

    @Min(value = 1, message = "重要程度范围为 1-5")
    @Max(value = 5, message = "重要程度范围为 1-5")
    private Integer importanceLevel = 1;

    @Min(value = 1, message = "考频范围为 1-5")
    @Max(value = 5, message = "考频范围为 1-5")
    private Integer examFrequency = 1;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sort = 0;

    @Min(value = 0, message = "启用状态不合法")
    @Max(value = 1, message = "启用状态不合法")
    private Integer isEnabled = 1;

    @Min(value = 1, message = "来源类型不合法")
    @Max(value = 3, message = "来源类型不合法")
    private Integer sourceType = 1;

    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;
}
