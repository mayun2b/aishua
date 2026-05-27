package zysy.iflytek.aishua.modules.user.entity.vo;

import lombok.Data;

/**
 * 用户视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class UserProfileVO {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer isAdmin;
}
