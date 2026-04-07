package zysy.iflytek.aishuai.user.vo;

import lombok.Data;

/**
 * 注册响应结果
 */
@Data
public class RegisterResponse {
    private Long userId; // 用户 ID
    private String phone; // 手机号
    private String nickname; // 昵称
    private String token; // JWT Token
}
