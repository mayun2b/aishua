package zysy.iflytek.aishua.modules.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.exam.entity.ExamPaperQuestion;

/**
 * 考试数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface ExamPaperQuestionMapper extends BaseMapper<ExamPaperQuestion> {
}
