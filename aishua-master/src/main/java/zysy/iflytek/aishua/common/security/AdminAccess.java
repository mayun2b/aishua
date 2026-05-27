package zysy.iflytek.aishua.common.security;

import org.springframework.stereotype.Component;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.user.entity.User;
import zysy.iflytek.aishua.modules.user.mapper.UserMapper;

/**
 * 用于校验当前用户是否具备管理员权限。
 */
@Component
public class AdminAccess {
    private final UserMapper userMapper;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public AdminAccess(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 执行参数与状态校验。
     */
    public void ensureAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在", 404);
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException("用户账号已禁用", 403);
        }
        if (!Integer.valueOf(1).equals(user.getIsAdmin())) {
            throw new BusinessException("需要管理员权限", 403);
        }
    }
}
