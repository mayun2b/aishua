package zysy.iflytek.aishua.modules.ai.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 智能问答数据传输对象，用于接口入参封装。
 */
@Data
public class PracticeQuestionAiSendMessageDTO {
    @NotBlank(message = "提问内容不能为空")
    @Size(max = 3000, message = "提问内容不能超过3000字符")
    private String content;

    @Size(max = 4000, message = "草稿答案不能超过4000字符")
    private String draftAnswer;

    @Min(value = 0, message = "作答耗时不能小于 0")
    private Integer timeCost;
}
