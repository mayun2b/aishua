package zysy.iflytek.aishua.modules.subject.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.subject.entity.Subject;

/**
 * 学科数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {
}
