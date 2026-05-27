package zysy.iflytek.aishua.modules.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.practice.entity.UserSubjectStats;

/**
 * 练习数据访问映射，定义该领域对外能力契约。
 */
@Mapper
public interface UserSubjectStatsMapper extends BaseMapper<UserSubjectStats> {
}
