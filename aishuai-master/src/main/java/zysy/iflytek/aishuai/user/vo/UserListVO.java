package zysy.iflytek.aishuai.user.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户列表返回信息
 */
@Data
public class UserListVO {
    private Long id; // 用户ID
    private String phone; // 手机号
    private String nickname; // 昵称
    private String avatar; // 头像URL
    private Integer status; // 状态：0-禁用，1-启用
    private Boolean isAdmin; // 是否为管理员
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}