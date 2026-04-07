package zysy.iflytek.aishuai.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import zysy.iflytek.aishuai.question.entity.Question;

/**
 * 题目 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    
    /**
     * 获取用户在指定知识点下完成的题目数
     */
    @Select("SELECT COUNT(DISTINCT er.question_id) FROM exercise_record er " +
            "JOIN question q ON er.question_id = q.id " +
            "WHERE er.user_id = #{userId} AND q.category_id = #{categoryId}")
    long getCompletedQuestionCount(Long userId, Long categoryId);
}
