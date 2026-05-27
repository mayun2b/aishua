package zysy.iflytek.aishua.modules.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;

/**
 * 标签数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface ExamTagMapper extends BaseMapper<ExamTag> {
}
