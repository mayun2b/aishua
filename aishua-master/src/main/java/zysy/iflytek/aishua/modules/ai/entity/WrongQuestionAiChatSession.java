package zysy.iflytek.aishua.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能问答实体对象，负责相关业务逻辑与流程处理。
 */
@Data
@TableName("wrong_question_ai_chat_session")
public class WrongQuestionAiChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionCode;

    private Long userId;

    private Long wrongQuestionId;

    private Long analysisId;

    private Long questionId;

    private Long subjectId;

    private Integer status;

    private Integer roundCount;

    private LocalDateTime lastMessageAt;

    private Integer totalPromptTokens;

    private Integer totalCompletionTokens;

    private Integer totalTokens;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
