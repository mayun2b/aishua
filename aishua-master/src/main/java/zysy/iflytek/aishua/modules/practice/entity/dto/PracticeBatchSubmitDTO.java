package zysy.iflytek.aishua.modules.practice.entity.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 练习数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeBatchSubmitDTO {
    /**
     * 客户端草稿基线版本号，用于乐观并发控制。
     */
    private Integer baseVersion;

    @Valid
    private List<PracticeAnswerItemDTO> answers = new ArrayList<>();
}
