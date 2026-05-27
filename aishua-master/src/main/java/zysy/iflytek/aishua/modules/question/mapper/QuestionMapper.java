package zysy.iflytek.aishua.modules.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.question.entity.Question;

/**
 * 题目数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
