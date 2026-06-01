package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能问答视图对象，用于接口出参封装。
 */
@Data
public class WrongQuestionAiChatSessionVO {
    private Long sessionId;

    private String sessionCode;

    private Long wrongQuestionId;

    private Long analysisId;

    private Integer status;

    private Integer roundCount;

    private LocalDateTime lastMessageAt;

    private Integer totalPromptTokens;

    private Integer totalCompletionTokens;

    private Integer totalTokens;

    private LocalDateTime createTime;
}
