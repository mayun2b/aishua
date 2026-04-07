package zysy.iflytek.aishuai.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String phone; // 用户名/手机号
    
    @NotBlank(message = "密码不能为空")
    private String password; // 密码
}