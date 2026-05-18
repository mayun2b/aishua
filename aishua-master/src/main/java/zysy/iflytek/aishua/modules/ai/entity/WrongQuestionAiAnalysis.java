package zysy.iflytek.aishua.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wrong_question_ai_analysis")
public class WrongQuestionAiAnalysis {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String analysisCode;

    private Long userId;

    private Long wrongQuestionId;

    private Long questionId;

    private Long subjectId;

    private Long directoryId;

    private Integer analysisType;

    private String contextSnapshot;

    private String resultJson;

    private String summary;

    private String modelProvider;

    private String modelName;

    private String promptVersion;

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
