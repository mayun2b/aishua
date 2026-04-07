package zysy.iflytek.aishuai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import zysy.iflytek.aishuai.user.entity.User;
import org.springframework.stereotype.Repository;

// 必须继承BaseMapper，否则无法注入sqlSessionFactory
@Repository // 可选：添加Repository注解，避免IDEA提示注入错误
public interface UserMapper extends BaseMapper<User> {
    // 自定义SQL方法（可选）
}