package zysy.iflytek.aishua.modules.ai.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 智能问答数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class WrongQuestionAiSendMessageDTO {
    @NotBlank(message = "提问内容不能为空")
    @Size(max = 3000, message = "提问内容不能超过3000字符")
    private String content;
}
