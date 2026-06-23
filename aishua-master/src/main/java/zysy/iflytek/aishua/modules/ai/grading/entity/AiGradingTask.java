package zysy.iflytek.aishua.modules.ai.grading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 评分任务实体。
 */
@Data
@TableName("ai_grading_task")
public class AiGradingTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizType;

    private Long bizRecordId;

    private Long questionId;

    private Long userId;

    private String status;

    private String triggerSource;

    private String requestPayloadJson;

    private String responsePayloadJson;

    private String errorMessage;

    private Integer retryCount;

    private Integer maxRetryCount;

    private LocalDateTime lockedAt;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
