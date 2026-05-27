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
@TableName("practice_question_ai_chat_message")
public class PracticeQuestionAiChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assistantSessionId;

    private Integer seqNo;

    private Integer role;

    private Integer messageType;

    private String contentText;

    private String contentJson;

    private String draftAnswerSnapshot;

    private Integer draftTimeCostSnapshot;

    private String modelProvider;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer latencyMs;

    private Integer status;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
