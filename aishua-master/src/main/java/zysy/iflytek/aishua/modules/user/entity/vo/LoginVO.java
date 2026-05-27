package zysy.iflytek.aishua.modules.user.entity.vo;

import lombok.Data;

/**
 * 登录响应视图对象。
 */
@Data
public class LoginVO {
    private String token;
    private UserVO user;

    /**
     * 用户信息视图对象。
     */
    @Data
    public static class UserVO {
        private Long id;
        private String phone;
        private String nickname;
        private String avatar;
        private Integer isAdmin;
    }
}