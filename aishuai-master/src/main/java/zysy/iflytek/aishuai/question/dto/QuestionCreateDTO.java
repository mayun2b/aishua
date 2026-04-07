package zysy.iflytek.aishuai.question.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 题目创建参数 DTO
 */
@Data
public class QuestionCreateDTO {
    @NotBlank(message = "题目标题不能为空")
    private String title; // 题目标题
    
    private String content; // 题目内容
    
    @NotNull(message = "题型不能为空")
    private Integer type; // 题型
    
    private Long categoryId; // 分类 ID
    
    private Long subjectId; // 学科 ID
    
    private Integer difficulty; // 难度等级
    
    private String options; // 选项（JSON）
    
    @NotBlank(message = "答案不能为空")
    private String answer; // 答案
    
    private String analysis; // 解析
    
    private String tags; // 标签
}
