package zysy.iflytek.aishuai.exercise.vo;

import lombok.Data;

import java.util.List;

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
    private Integer categoryCount; // 涉及分类数
    private List<CategoryStatsVO> categoryStats; // 各分类统计
    
    @Data
    public static class CategoryStatsVO {
        private String categoryName; // 分类名称
        private Integer totalCount; // 该分类总做题数
        private Integer correctCount; // 该分类正确数
        private Double correctRate; // 该分类正确率
    }
}
