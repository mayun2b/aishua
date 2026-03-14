package zysy.iflytek.aishuai.user.vo;

import lombok.Data;

/**
 * 当前登录用户信息响应
 */
@Data
public class UserInfoResponse {
    private Long userId;
    private String phone;
    private String nickname;
    private String avatar;
    private Boolean isAdmin;
}

