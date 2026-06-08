package zysy.iflytek.aishua.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_analysis_report")
public class LearningAnalysisReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String reportCode;

    private Long userId;

    private Long subjectId;

    private String subjectName;

    private String grade;

    private String textbookVersion;

    private String queryText;

    private String summary;

    private String fullText;

    private String resultJson;

    private String rawResponseJson;

    private Integer dataQualitySufficient;

    private String missingMetrics;

    private String workflowRunId;

    private String taskId;

    private Integer status;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
