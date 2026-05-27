package zysy.iflytek.aishua.modules.user.entity.dto;

import lombok.Data;

/**
 * 注册请求数据传输对象。
 */
@Data
public class RegisterDTO {
    private String phone;
    private String password;
    private String nickname;
}