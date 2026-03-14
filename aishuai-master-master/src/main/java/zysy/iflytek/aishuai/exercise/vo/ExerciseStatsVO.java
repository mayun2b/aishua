package zysy.iflytek.aishuai.exercise.vo;

import lombok.Data;

/**
 * 练习统计 VO
 */
@Data
public class ExerciseStatsVO {
    private Integer totalCount; // 总做题数
    private Integer correctCount; // 正确数
    private Integer wrongCount; // 错误数
    private Double correctRate; // 正确率
    private Integer continuousDays; // 连续天数
}
