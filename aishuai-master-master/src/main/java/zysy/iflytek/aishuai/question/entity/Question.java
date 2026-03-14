package zysy.iflytek.aishuai.question.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键 ID
    
    @TableField("title")
    private String title; // 题目标题/题干
    
    @TableField("content")
    private String content; // 题目内容
    
    @TableField("type")
    private Integer type; // 题型：1-单选，2-多选，3-判断，4-填空，5-简答
    
    @TableField("category_id")
    private Long categoryId; // 分类 ID
    
    @TableField("difficulty")
    private Integer difficulty; // 难度等级：1-简单，2-中等，3-困难
    
    @TableField("options")
    private String options; // 选择题选项（JSON 格式）
    
    @TableField("answer")
    private String answer; // 正确答案
    
    @TableField("analysis")
    private String analysis; // 答案解析
    
    @TableField("tags")
    private String tags; // 知识点标签（逗号分隔）
    
    @TableField("correct_rate")
    private Double correctRate; // 正确率
    
    @TableField("do_count")
    private Integer doCount; // 做题次数
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
