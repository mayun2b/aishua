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
 * 用户控制器，提供该领域对外接口入口。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userService.login(loginDTO));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping("/register")
    public Result<UserProfileVO> register(@RequestBody RegisterDTO registerDTO) {
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userService.register(registerDTO));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        // 调用服务层处理业务并封装统一响应。
        return Result.success(userService.getProfile(UserContext.requireUserId()));
    }
}
