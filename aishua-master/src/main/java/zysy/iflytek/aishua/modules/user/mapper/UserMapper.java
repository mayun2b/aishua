package zysy.iflytek.aishua.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import zysy.iflytek.aishua.modules.user.entity.User;

/**
 * 用户数据访问映射。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}