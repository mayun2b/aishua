package zysy.iflytek.aishua.modules.practice.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习实体对象，用于持久化字段映射与数据承载。
 */
@Data
@TableName("exercise_record")
public class ExerciseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Long sessionRefId;

    private Integer exerciseMode;

    private String userAnswer;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Integer isCorrect;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String aiGradingStatus;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Double aiGradingConfidence;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String aiGradingFeedback;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String aiGradingDetailJson;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String aiGradingErrorMessage;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime aiGradedAt;

    private Integer timeCost;

    private LocalDateTime exerciseTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
