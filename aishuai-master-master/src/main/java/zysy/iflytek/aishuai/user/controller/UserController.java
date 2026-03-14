package zysy.iflytek.aishuai.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.user.dto.LoginRequest;
import zysy.iflytek.aishuai.user.dto.RegisterRequest;
import zysy.iflytek.aishuai.user.dto.UserDeleteRequest;
import zysy.iflytek.aishuai.user.dto.UserQueryRequest;
import zysy.iflytek.aishuai.user.entity.User;
import zysy.iflytek.aishuai.user.service.UserService;
import zysy.iflytek.aishuai.user.vo.LoginResponse;
import zysy.iflytek.aishuai.user.vo.RegisterResponse;
import zysy.iflytek.aishuai.user.vo.UserInfoResponse;
import zysy.iflytek.aishuai.user.vo.UserListVO;

import java.util.Objects;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request.getPhone(), request.getPassword());
            return Result.success(response);
        } catch (RuntimeException e) {
            log.error("登录失败：{}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = userService.register(
                request.getPhone(), 
                request.getPassword(), 
                request.getNickname()
            );
            return Result.success(response);
        } catch (RuntimeException e) {
            log.error("注册失败：{}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 管理员查询用户列表接口
     */
    @PostMapping("/list")
    public Result<Page<UserListVO>> listUsers(@RequestBody UserQueryRequest request,
                                             HttpServletRequest httpRequest) {
        try {
            // 从请求属性中获取当前用户ID
            Long currentUserId = (Long) httpRequest.getAttribute("userId");
            if (currentUserId == null) {
                return Result.fail("无效的认证信息");
            }
            
            Page<UserListVO> userList = userService.listUsers(request, currentUserId);
            return Result.success(userList);
        } catch (RuntimeException e) {
            log.error("查询用户列表失败：{}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 管理员禁用用户接口（将status设为0）
     */
    @PostMapping("/disable")
    public Result<Boolean> disableUser(@RequestBody UserDeleteRequest request,
                                      HttpServletRequest httpRequest) {
        try {
            // 从请求属性中获取当前用户ID
            Long currentUserId = (Long) httpRequest.getAttribute("userId");
            if (currentUserId == null) {
                return Result.fail("无效的认证信息");
            }
            
            boolean success = userService.disableUser(request.getUserId(), currentUserId);
            return Result.success(success);
        } catch (RuntimeException e) {
            log.error("禁用用户失败：{}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(HttpServletRequest httpRequest) {
        Long currentUserId = (Long) httpRequest.getAttribute("userId");
        if (currentUserId == null) {
            return Result.unauth("请先登录");
        }

        User user = userService.getById(currentUserId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return Result.fail("用户不存在");
        }

        UserInfoResponse resp = new UserInfoResponse();
        resp.setUserId(user.getId());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setIsAdmin(user.getIsAdmin() != null && user.getIsAdmin() == 1);
        return Result.success(resp);
    }
}