package zysy.iflytek.aishua.common.security;

import org.springframework.stereotype.Component;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.user.entity.User;
import zysy.iflytek.aishua.modules.user.mapper.UserMapper;

@Component
public class AdminAccess {
    private final UserMapper userMapper;

    public AdminAccess(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void ensureAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在", 404);
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用", 403);
        }
        if (!Integer.valueOf(1).equals(user.getIsAdmin())) {
            throw new BusinessException("需要管理员权限", 403);
        }
    }
}
