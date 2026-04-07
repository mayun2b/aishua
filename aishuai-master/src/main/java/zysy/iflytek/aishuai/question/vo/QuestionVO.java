package zysy.iflytek.aishuai.question.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题目视图对象
 */
@Data
public class QuestionVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private Long categoryId;
    private String categoryName; // 分类名称
    private Long subjectId; // 学科 ID
    private String subjectName; // 学科名称
    private Integer difficulty;
    private String options;
    private String answer;
    private String analysis;
    private String tags;
    private Double correctRate;
    private Integer doCount;
    private LocalDateTime createTime;
}
