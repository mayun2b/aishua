package zysy.iflytek.aishuai.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户删除请求参数
 */
@Data
public class UserDeleteRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId; // 要删除的用户ID
}