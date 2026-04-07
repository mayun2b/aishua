package zysy.iflytek.aishuai.wrong.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 错题实体
 */
@Data
@TableName("wrong_question")
public class WrongQuestion {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键 ID
    
    @TableField("user_id")
    private Long userId; // 用户 ID
    
    @TableField("question_id")
    private Long questionId; // 题目 ID
    
    @TableField("wrong_count")
    private Integer wrongCount; // 错误次数
    
    @TableField("last_wrong_time")
    private LocalDateTime lastWrongTime; // 最后错误时间
    
    @TableField("master_status")
    private Integer masterStatus; // 掌握状态：0-未掌握，1-已掌握
    
    @TableField("create_time")
    private LocalDateTime createTime; // 创建时间
    
    @TableField("update_time")
    private LocalDateTime updateTime; // 更新时间
}
