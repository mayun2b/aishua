package zysy.iflytek.aishuai.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zysy.iflytek.aishuai.user.dto.UserQueryRequest;
import zysy.iflytek.aishuai.user.entity.User;
import zysy.iflytek.aishuai.user.vo.LoginResponse;
import zysy.iflytek.aishuai.user.vo.RegisterResponse;
import zysy.iflytek.aishuai.user.vo.UserListVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码
     * @return 登录响应信息
     */
    LoginResponse login(String phone, String password);

    /**
     * 用户注册
     * @param phone 手机号
     * @param password 密码
     * @param nickname 昵称
     * @return 注册响应信息
     */
    RegisterResponse register(String phone, String password, String nickname);
    
    /**
     * 管理员查询用户列表
     * @param request 查询条件
     * @param currentUserId 当前操作用户ID（用于权限校验）
     * @return 分页用户列表
     */
    Page<UserListVO> listUsers(UserQueryRequest request, Long currentUserId);
    
    /**
     * 管理员禁用用户（将status设为0）
     * @param userId 要禁用的用户ID
     * @param currentUserId 当前操作用户ID（用于权限校验）
     * @return 是否禁用成功
     */
    boolean disableUser(Long userId, Long currentUserId);
}