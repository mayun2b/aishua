package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

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
