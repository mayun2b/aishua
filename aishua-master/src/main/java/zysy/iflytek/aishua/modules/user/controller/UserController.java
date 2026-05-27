package zysy.iflytek.aishua.modules.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.user.entity.dto.LoginDTO;
import zysy.iflytek.aishua.modules.user.entity.dto.RegisterDTO;
import zysy.iflytek.aishua.modules.user.entity.vo.LoginVO;
import zysy.iflytek.aishua.modules.user.entity.vo.UserProfileVO;
import zysy.iflytek.aishua.modules.user.service.UserService;

/**
 * 用户控制器，负责相关业务逻辑与流程处理。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 处理业务请求并返回结果。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userService.login(loginDTO));
    }

    /**
     * 处理业务请求并返回结果。
     */
    @PostMapping("/register")
    public Result<UserProfileVO> register(@RequestBody RegisterDTO registerDTO) {
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userService.register(registerDTO));
    }

    /**
     * 处理业务请求并返回结果。
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userService.getProfile(UserContext.requireUserId()));
    }
}
