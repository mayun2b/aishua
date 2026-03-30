package zysy.iflytek.aishuai.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;
import zysy.iflytek.aishuai.stats.entity.UserStats;

import java.util.List;

/**
 * 用户统计 Mapper 接口
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {
    
    /**
     * 查询用户涉及的分类数量
     */
    @Select("""
        SELECT 
            COUNT(DISTINCT qc.id)
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            question_category qc ON q.category_id = qc.id
        WHERE 
            er.user_id = #{userId}
        """)
    Integer getCategoryCount(Long userId);
    
    /**
     * 查询用户的科目统计数据
     */
    @Select("""
        SELECT 
            s.id as subjectId, 
            s.name as subjectName, 
            COUNT(er.id) as totalCount, 
            SUM(er.is_correct) as correctCount,
            CASE 
                WHEN COUNT(er.id) > 0 THEN (SUM(er.is_correct) * 100.0 / COUNT(er.id)) 
                ELSE 0.0 
            END as correctRate
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            subject s ON q.subject_id = s.id
        WHERE 
            er.user_id = #{userId}
        GROUP BY 
            s.id, s.name
        ORDER BY 
            totalCount DESC
        """)
    List<ExerciseStatsVO.SubjectStatsVO> getSubjectStats(Long userId);
    
    /**
     * 查询用户涉及的科目数量
     */
    @Select("""
        SELECT 
            COUNT(DISTINCT s.id)
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            subject s ON q.subject_id = s.id
        WHERE 
            er.user_id = #{userId}
        """)
    Integer getSubjectCount(Long userId);
    
    /**
     * 查询用户的分类统计数据（包含科目ID）
     */
    @Select("""
        SELECT 
            qc.name as categoryName, 
            q.subject_id as subjectId,
            COUNT(er.id) as totalCount, 
            SUM(er.is_correct) as correctCount,
            CASE 
                WHEN COUNT(er.id) > 0 THEN (SUM(er.is_correct) * 100.0 / COUNT(er.id)) 
                ELSE 0.0 
            END as correctRate
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            question_category qc ON q.category_id = qc.id
        WHERE 
            er.user_id = #{userId}
        GROUP BY 
            qc.id, qc.name, q.subject_id
        ORDER BY 
            totalCount DESC
        """)
    List<ExerciseStatsVO.CategoryStatsVO> getCategoryStats(Long userId);
    
    /**
     * 查询用户的薄弱知识点（正确率低于40%且总练习次数>=5）
     */
    @Select("""
        SELECT 
            qc.name as categoryName, 
            q.subject_id as subjectId,
            s.name as subjectName,
            COUNT(er.id) as totalCount, 
            SUM(er.is_correct) as correctCount,
            CASE 
                WHEN COUNT(er.id) > 0 THEN (SUM(er.is_correct) * 100.0 / COUNT(er.id)) 
                ELSE 0.0 
            END as correctRate
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            question_category qc ON q.category_id = qc.id
        JOIN 
            subject s ON q.subject_id = s.id
        WHERE 
            er.user_id = #{userId}
        GROUP BY 
            qc.id, qc.name, q.subject_id, s.name
        HAVING 
            COUNT(er.id) >= 5 
            AND (SUM(er.is_correct) * 100.0 / COUNT(er.id)) < 40.0
        ORDER BY 
            correctRate ASC
        """)
    List<ExerciseStatsVO.WeakPointStatsVO> getWeakPointStats(Long userId);
    
    /**
     * 查询指定学科的总题数
     */
    @Select("""
        SELECT 
            COUNT(er.id)
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        WHERE 
            er.user_id = #{userId}
            AND q.subject_id = #{subjectId}
        """)
    Long getSubjectTotalCount(Long userId, Long subjectId);
    
    /**
     * 查询指定学科的正确题数
     */
    @Select("""
        SELECT 
            SUM(er.is_correct)
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        WHERE 
            er.user_id = #{userId}
            AND q.subject_id = #{subjectId}
        """)
    Long getSubjectCorrectCount(Long userId, Long subjectId);
    
    /**
     * 查询指定学科的分类统计数据
     */
    @Select("""
        SELECT 
            qc.name as categoryName, 
            q.subject_id as subjectId,
            COUNT(er.id) as totalCount, 
            SUM(er.is_correct) as correctCount,
            CASE 
                WHEN COUNT(er.id) > 0 THEN (SUM(er.is_correct) * 100.0 / COUNT(er.id)) 
                ELSE 0.0 
            END as correctRate
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            question_category qc ON q.category_id = qc.id
        WHERE 
            er.user_id = #{userId}
            AND q.subject_id = #{subjectId}
        GROUP BY 
            qc.id, qc.name, q.subject_id
        ORDER BY 
            totalCount DESC
        """)
    List<ExerciseStatsVO.CategoryStatsVO> getCategoryStatsBySubject(Long userId, Long subjectId);
    
    /**
     * 查询指定学科涉及的分类数量
     */
    @Select("""
        SELECT 
            COUNT(DISTINCT qc.id)
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            question_category qc ON q.category_id = qc.id
        WHERE 
            er.user_id = #{userId}
            AND q.subject_id = #{subjectId}
        """)
    Integer getCategoryCountBySubject(Long userId, Long subjectId);
    
    /**
     * 查询指定学科的薄弱知识点
     */
    @Select("""
        SELECT 
            qc.name as categoryName, 
            q.subject_id as subjectId,
            s.name as subjectName,
            COUNT(er.id) as totalCount, 
            SUM(er.is_correct) as correctCount,
            CASE 
                WHEN COUNT(er.id) > 0 THEN (SUM(er.is_correct) * 100.0 / COUNT(er.id)) 
                ELSE 0.0 
            END as correctRate
        FROM 
            exercise_record er
        JOIN 
            question q ON er.question_id = q.id
        JOIN 
            question_category qc ON q.category_id = qc.id
        JOIN 
            subject s ON q.subject_id = s.id
        WHERE 
            er.user_id = #{userId}
            AND q.subject_id = #{subjectId}
        GROUP BY 
            qc.id, qc.name, q.subject_id, s.name
        HAVING 
            COUNT(er.id) >= 5 
            AND (SUM(er.is_correct) * 100.0 / COUNT(er.id)) < 40.0
        ORDER BY 
            correctRate ASC
        """)
    List<ExerciseStatsVO.WeakPointStatsVO> getWeakPointStatsBySubject(Long userId, Long subjectId);
}
