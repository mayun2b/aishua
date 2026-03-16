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
     * 查询用户的分类统计数据
     */
    @Select("""
        SELECT 
            qc.name as categoryName, 
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
            qc.id, qc.name
        ORDER BY 
            totalCount DESC
        """)
    List<ExerciseStatsVO.CategoryStatsVO> getCategoryStats(Long userId);
    
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
}
