package zysy.iflytek.aishua.modules.user.entity.vo;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer isAdmin;
}
