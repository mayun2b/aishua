package zysy.iflytek.aishuai.user.dto;

import lombok.Data;

/**
 * 用户查询请求参数
 */
@Data
public class UserQueryRequest {
    private String phone; // 手机号（模糊查询）
    private String nickname; // 昵称（模糊查询）
    private Integer status; // 状态：0-禁用，1-启用
    private Integer page = 1; // 页码，默认第1页
    private Integer size = 10; // 每页大小，默认10条
}