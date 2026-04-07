package zysy.iflytek.aishuai.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user") // 数据库表名
public class User {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键 ID
    
    @TableField("phone")
    private String phone; // 手机号（唯一）

    @TableField("password")
    private String password; // 密码（MD5加密）

    @TableField("nickname")
    private String nickname; // 昵称

    @TableField("avatar")
    private String avatar; // 头像URL

    @TableField("status")
    private Integer status; // 状态：0-禁用，1-启用

    @TableField("is_admin")
    private Integer isAdmin; // 管理员标识：0-普通用户，1-管理员

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间

    @TableLogic // 逻辑删除
    @TableField("deleted")
    private Integer deleted; // 0-未删除，1-已删除
}