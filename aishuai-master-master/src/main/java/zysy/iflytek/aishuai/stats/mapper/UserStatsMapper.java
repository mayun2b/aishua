package zysy.iflytek.aishuai.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishuai.stats.entity.UserStats;

/**
 * 用户统计 Mapper 接口
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {
}
