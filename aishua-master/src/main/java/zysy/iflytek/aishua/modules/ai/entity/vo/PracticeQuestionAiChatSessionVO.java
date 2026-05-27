package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能问答视图对象，负责相关业务逻辑与流程处理。
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
