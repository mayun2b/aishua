package zysy.iflytek.aishuai.stats.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户统计实体
 */
@Data
@TableName("user_stats")
public class UserStats {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键 ID
    
    @TableField("user_id")
    private Long userId; // 用户 ID
    
    @TableField("total_count")
    private Integer totalCount; // 总做题数
    
    @TableField("correct_count")
    private Integer correctCount; // 正确数
    
    @TableField("wrong_count")
    private Integer wrongCount; // 错误数
    
    @TableField("continuous_days")
    private Integer continuousDays; // 连续天数
    
    @TableField("max_continuous_days")
    private Integer maxContinuousDays; // 最大连续天数
    
    @TableField("last_exercise_date")
    private LocalDate lastExerciseDate; // 最后练习日期
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
