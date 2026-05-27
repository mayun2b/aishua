package zysy.iflytek.aishua.modules.ai.entity.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 智能问答数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class WrongQuestionAiAnalysisRequestDTO {
    @Size(max = 500, message = "补充说明长度不能超过500字符")
    private String extraInstruction;
}
