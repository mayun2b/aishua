package zysy.iflytek.aishuai.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题目分类实体
 */
@Data
@TableName("question_category")
public class QuestionCategory {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键 ID
    
    @TableField("name")
    private String name; // 分类名称
    
    @TableField("parent_id")
    private Long parentId; // 父分类 ID
    
    @TableField("sort")
    private Integer sort; // 排序值
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
