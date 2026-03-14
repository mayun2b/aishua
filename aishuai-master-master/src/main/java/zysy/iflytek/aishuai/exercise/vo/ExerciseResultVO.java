package zysy.iflytek.aishuai.exercise.vo;

import lombok.Data;

/**
 * 答题结果 VO
 */
@Data
public class ExerciseResultVO {
    private Long recordId; // 记录 ID
    private Boolean isCorrect; // 是否正确
    private String correctAnswer; // 正确答案
    private String analysis; // 解析
    private Integer timeCost; // 耗时
}
