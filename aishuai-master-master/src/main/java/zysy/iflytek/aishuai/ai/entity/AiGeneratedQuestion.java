package zysy.iflytek.aishuai.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI生成题目实体类
 */
@Data
@TableName("ai_generated_question")
public class AiGeneratedQuestion {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目标题/题干
     */
    private String title;

    /**
     * 题目内容（支持富文本、公式等）
     */
    private String content;

    /**
     * 题型：1-单选，2-多选，3-判断，4-填空，5-简答
     */
    private Integer type;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 学科ID
     */
    private Long subjectId;

    /**
     * 难度等级：1-简单，2-中等，3-困难
     */
    private Integer difficulty;

    /**
     * 选择题选项（JSON格式）
     */
    private String options;

    /**
     * 正确答案
     */
    private String answer;

    /**
     * 答案解析
     */
    private String analysis;

    /**
     * 知识点标签（逗号分隔）
     */
    private String tags;

    /**
     * 生成原因（基于哪些薄弱知识点）
     */
    private String generateReason;

    /**
     * 生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 是否已练习：0-未练习，1-已练习
     */
    private Integer isPracticed;
}