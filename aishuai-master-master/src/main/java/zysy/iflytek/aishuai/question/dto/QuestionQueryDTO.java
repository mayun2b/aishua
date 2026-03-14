package zysy.iflytek.aishuai.question.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 题目查询参数 DTO
 */
@Data
public class QuestionQueryDTO {
    private Long categoryId; // 分类 ID
    private Long subjectId; // 学科 ID
    private Integer type; // 题型
    private Integer difficulty; // 难度等级
    private String tags; // 标签
    private Integer pageNum = 1; // 页码
    private Integer pageSize = 10; // 每页数量
}
