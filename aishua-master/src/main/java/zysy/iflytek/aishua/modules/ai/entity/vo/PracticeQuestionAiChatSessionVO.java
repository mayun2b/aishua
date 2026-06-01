package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能问答视图对象，用于接口出参封装。
 */
@Data
public class PracticeQuestionAiChatSessionVO {
    private Long sessionId;

    private String sessionCode;

    private Long practiceSessionId;

    private Long questionId;

    private Long subjectId;

    private Integer triggerSource;

    private Integer status;

    private Integer roundCount;

    private LocalDateTime lastMessageAt;

    private Integer totalPromptTokens;

    private Integer totalCompletionTokens;

    private Integer totalTokens;

    private LocalDateTime createTime;
}
