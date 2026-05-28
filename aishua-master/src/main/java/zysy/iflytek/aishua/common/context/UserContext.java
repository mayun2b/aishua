package zysy.iflytek.aishua.common.context;

import zysy.iflytek.aishua.common.exception.BusinessException;

/**
 * Request-scoped user context based on ThreadLocal.
 */
public final class UserContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> AUTHORIZATION_HOLDER = new ThreadLocal<>();


// 构造方法，负责注入依赖组件。
    private UserContext() {
    }

// 保存并更新业务处理结果。
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }


// 查询并返回处理结果。
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }


// 执行参数与状态校验。
    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new BusinessException("用户上下文缺失，请重新登录", 401);
        }
        return userId;
    }


// 请求结束后必须调用，避免线程本地存储泄漏。
    public static void setAuthorization(String authorization) {
        AUTHORIZATION_HOLDER.set(authorization);
    }

    public static String getAuthorization() {
        return AUTHORIZATION_HOLDER.get();
    }

    public static String requireAuthorization() {
        String authorization = getAuthorization();
        if (authorization == null || authorization.isBlank()) {
            throw new BusinessException("登录令牌缺失", 401);
        }
        return authorization;
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        AUTHORIZATION_HOLDER.remove();
    }
}
