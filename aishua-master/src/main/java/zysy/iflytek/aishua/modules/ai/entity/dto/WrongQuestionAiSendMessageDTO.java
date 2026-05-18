package zysy.iflytek.aishua.modules.ai.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WrongQuestionAiSendMessageDTO {
    @NotBlank(message = "提问内容不能为空")
    @Size(max = 3000, message = "提问内容不能超过3000字符")
    private String content;
}
