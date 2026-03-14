package zysy.iflytek.aishuai.exercise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 练习记录实体
 */
@Data
@TableName("exercise_record")
public class ExerciseRecord {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键 ID
    
    @TableField("user_id")
    private Long userId; // 用户 ID
    
    @TableField("question_id")
    private Long questionId; // 题目 ID
    
    @TableField("user_answer")
    private String userAnswer; // 用户答案
    
    @TableField("is_correct")
    private Integer isCorrect; // 是否正确：0-错误，1-正确
    
    @TableField("time_cost")
    private Integer timeCost; // 耗时（秒）
    
    @TableField("exercise_mode")
    private Integer exerciseMode; // 练习模式：1-顺序，2-随机，3-错题，4-限时
    
    @TableField("exercise_time")
    private LocalDateTime exerciseTime; // 练习时间
    
    @TableField("session_id")
    private String sessionId; // 练习会话 ID
}
