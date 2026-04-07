package zysy.iflytek.aishuai.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishuai.question.entity.QuestionCategory;

/**
 * 题目分类 Mapper 接口
 */
@Mapper
public interface QuestionCategoryMapper extends BaseMapper<QuestionCategory> {
}
