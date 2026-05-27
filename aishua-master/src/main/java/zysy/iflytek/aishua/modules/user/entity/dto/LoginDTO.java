package zysy.iflytek.aishua.modules.user.entity.dto;

import lombok.Data;

/**
 * 登录请求数据传输对象。
 */
@Data
public class LoginDTO {
    private String phone;
    private String password;
}