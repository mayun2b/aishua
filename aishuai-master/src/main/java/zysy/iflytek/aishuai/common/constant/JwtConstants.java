package zysy.iflytek.aishuai.common.constant;

/**
 * JWT相关常量
 */
public class JwtConstants {
    // Token请求头
    public static final String TOKEN_HEADER = "Authorization";
    // Token前缀
    public static final String TOKEN_PREFIX = "Bearer ";
    // Token过期时间（24小时）
    public static final Long EXPIRATION = 86400000L;
    // 密钥（建议替换为自己的随机长字符串）
    public static final String SECRET = "aishuaiSecretKey1234567890abcdefghijklmnopqrstuvwxyz";
}