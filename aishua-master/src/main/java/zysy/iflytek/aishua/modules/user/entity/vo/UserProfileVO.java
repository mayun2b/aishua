package zysy.iflytek.aishua.modules.user.entity.vo;

import lombok.Data;

/**
 * 用户视图对象，用于接口出参封装。
 */
@Data
public class UserProfileVO {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer isAdmin;
}
