package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 练习数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeDraftSaveDTO {
    @NotNull(message = "草稿版本不能为空")
    @Min(value = 0, message = "草稿版本不能小于 0")
    private Integer baseVersion;

    @Valid
    private List<PracticeAnswerItemDTO> changes = new ArrayList<>();
}
