package zysy.iflytek.aishua.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能问答实体对象，用于持久化字段映射与数据承载。
 */
@Data
@TableName("wrong_question_ai_chat_message")
public class WrongQuestionAiChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Integer seqNo;

    private Integer role;

    private Integer messageType;

    private String contentText;

    private String contentJson;

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
