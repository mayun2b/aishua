package zysy.iflytek.aishua.modules.ai.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WrongQuestionAiChatMessageVO {
    private Long messageId;

    private Long sessionId;

    private Integer seqNo;

    private Integer role;

    private String roleName;

    private String content;

    private Integer status;

    private String errorMessage;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer latencyMs;

    private LocalDateTime createTime;
}
