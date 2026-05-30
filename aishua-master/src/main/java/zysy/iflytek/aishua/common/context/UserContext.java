package zysy.iflytek.aishua.common.context;

import zysy.iflytek.aishua.common.exception.BusinessException;

/**
 * 请求级用户上下文。
 * 使用 ThreadLocal 保存当前请求解析出的 userId 与 token。
 */
public final class UserContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> AUTHORIZATION_HOLDER = new ThreadLocal<>();

    /**
     * 工具类不允许实例化。
     */
    private UserContext() {
    }

    /**
     * 写入当前请求的用户 ID。
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前请求的用户 ID。
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 获取当前请求用户 ID，若不存在则抛出 401 业务异常。
     */
    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new BusinessException("用户上下文缺失，请重新登录", 401);
        }
        return userId;
    }

    /**
     * 写入当前请求的令牌值（通常是去掉 Bearer 前缀后的 token）。
     */
    public static void setAuthorization(String authorization) {
        AUTHORIZATION_HOLDER.set(authorization);
    }

    /**
     * 获取当前请求的令牌值。
     */
    public static String getAuthorization() {
        return AUTHORIZATION_HOLDER.get();
    }

    /**
     * 获取当前请求令牌，若不存在则抛出 401 业务异常。
     */
    public static String requireAuthorization() {
        String authorization = getAuthorization();
        if (authorization == null || authorization.isBlank()) {
            throw new BusinessException("登录令牌缺失", 401);
        }
        return authorization;
    }

    /**
     * 请求结束时清理 ThreadLocal，避免线程复用导致数据串请求。
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        AUTHORIZATION_HOLDER.remove();
    }
}
