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
    private Integer subjectCount; // 涉及科目数
    private List<CategoryStatsVO> categoryStats; // 各分类统计
    private List<SubjectStatsVO> subjectStats; // 各科目统计
    private List<WeakPointStatsVO> weakPointStats; // 薄弱知识点统计
    
    @Data
    public static class CategoryStatsVO {
        private String categoryName; // 分类名称
        private Long subjectId; // 所属科目ID
        private Integer totalCount; // 该分类总做题数
        private Integer correctCount; // 该分类正确数
        private Double correctRate; // 该分类正确率
    }
    
    @Data
    public static class SubjectStatsVO {
        private Long subjectId; // 科目ID
        private String subjectName; // 科目名称
        private Integer totalCount; // 该科目总做题数
        private Integer correctCount; // 该科目正确数
        private Double correctRate; // 该科目正确率
    }
    
    /**
     * 薄弱知识点统计VO
     */
    @Data
    public static class WeakPointStatsVO {
        private String categoryName; // 知识点名称
        private Long subjectId; // 所属科目ID
        private String subjectName; // 所属科目名称
        private Integer totalCount; // 总做题数
        private Integer correctCount; // 正确数
        private Double correctRate; // 正确率
    }
}
