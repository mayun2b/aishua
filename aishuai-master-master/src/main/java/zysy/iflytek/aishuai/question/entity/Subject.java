package zysy.iflytek.aishuai.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学科实体
 */
@Data
@TableName("subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键 ID
    
    @TableField("name")
    private String name; // 学科名称
    
    @TableField("code")
    private String code; // 学科代码
    
    @TableField("description")
    private String description; // 学科描述
    
    @TableField("sort")
    private Integer sort; // 排序值
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}