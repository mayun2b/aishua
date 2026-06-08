package zysy.iflytek.aishua.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("learning_analysis_knowledge_point")
public class LearningAnalysisKnowledgePoint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;

    private String name;

    private String moduleName;

    private String reasonText;

    private String masteryLevel;

    private BigDecimal correctRate;

    private Integer sampleCount;

    private Integer wrongCount;

    private Integer priority;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
