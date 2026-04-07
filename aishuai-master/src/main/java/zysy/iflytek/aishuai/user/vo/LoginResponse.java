package zysy.iflytek.aishuai.user.vo;

import lombok.Data;

/**
 * 登录响应结果
 */
@Data
public class LoginResponse {
    private String token; // JWT Token
    private Long userId; // 用户 ID
    private String phone; // 手机号
    private String nickname; // 昵称
    private String avatar; // 头像 URL
    private Boolean isAdmin; // 是否为管理员
}