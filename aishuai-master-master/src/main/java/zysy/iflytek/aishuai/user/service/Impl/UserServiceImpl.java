package zysy.iflytek.aishuai.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import zysy.iflytek.aishuai.user.dto.UserQueryRequest;
import zysy.iflytek.aishuai.user.entity.User;
import zysy.iflytek.aishuai.user.mapper.UserMapper;
import zysy.iflytek.aishuai.user.service.UserService;
import zysy.iflytek.aishuai.user.vo.LoginResponse;
import zysy.iflytek.aishuai.user.vo.RegisterResponse;
import zysy.iflytek.aishuai.user.vo.UserListVO;
import zysy.iflytek.aishuai.utils.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public LoginResponse login(String phone, String password) {
        // 1. 查询用户（启用状态 + 未删除）
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getStatus, 1)
                .eq(User::getDeleted, 0));
    
        // 2. 校验用户是否存在
        if (user == null) {
            throw new RuntimeException("手机号不存在或账号已禁用");
        }
    
        // 3. 校验密码（MD5 加密对比）
        String encryptPwd = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!encryptPwd.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
    
        // 4. 生成 JWT Token
        String token = JwtUtil.generateToken(user.getId());
    
        // 5. 封装返回结果
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setIsAdmin(user.getIsAdmin() != null && user.getIsAdmin() == 1);
    
        return response;
    }

    @Override
    public RegisterResponse register(String phone, String password, String nickname) {
        // 1. 检查手机号是否已存在
        User existingUser = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getDeleted, 0));
        
        if (existingUser != null) {
            throw new RuntimeException("手机号已被注册");
        }

        // 3. 创建新用户
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setIsAdmin(0); // 默认普通用户
        
        // 4. MD5 加密密码
        String encryptPwd = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        user.setPassword(encryptPwd);
        
        // 5. 设置默认状态
        user.setStatus(1); // 启用状态
        user.setAvatar(""); // 默认空头像
        
        // 6. 保存用户
        boolean saved = this.save(user);
        if (!saved) {
            throw new RuntimeException("注册失败，请稍后重试");
        }

        // 7. 生成 JWT Token
        String token = JwtUtil.generateToken(user.getId());

        // 8. 封装返回结果
        RegisterResponse response = new RegisterResponse();
        response.setUserId(user.getId());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setToken(token);

        log.info("用户注册成功：{}", phone);
        return response;
    }
    
    @Override
    public Page<UserListVO> listUsers(UserQueryRequest request, Long currentUserId) {
        // 1. 权限校验：只有管理员才能查询用户列表
        User currentUser = this.getById(currentUserId);
        if (currentUser == null || currentUser.getIsAdmin() == null || currentUser.getIsAdmin() != 1) {
            throw new RuntimeException("权限不足，只有管理员才能管理用户");
        }
        
        // 2. 构建查询条件（MyBatis-Plus会自动处理逻辑删除条件）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            queryWrapper.like(User::getPhone, request.getPhone().trim());
        }
        
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            queryWrapper.like(User::getNickname, request.getNickname().trim());
        }
        
        if (request.getStatus() != null) {
            queryWrapper.eq(User::getStatus, request.getStatus());
        }
        
        // 3. 执行分页查询
        Page<User> page = new Page<>(request.getPage(), request.getSize());
        IPage<User> userPage = this.page(page, queryWrapper);
        
        // 4. 转换为VO对象
        Page<UserListVO> resultPage = new Page<>();
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        resultPage.setTotal(userPage.getTotal());
        resultPage.setPages(userPage.getPages());
        
        resultPage.setRecords(userPage.getRecords().stream().map(user -> {
            UserListVO vo = new UserListVO();
            vo.setId(user.getId());
            vo.setPhone(user.getPhone());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setStatus(user.getStatus());
            vo.setIsAdmin(user.getIsAdmin() != null && user.getIsAdmin() == 1);
            vo.setCreateTime(user.getCreateTime());
            vo.setUpdateTime(user.getUpdateTime());
            return vo;
        }).toList());
        
        return resultPage;
    }
    
    @Override
    public boolean disableUser(Long userId, Long currentUserId) {
        // 1. 权限校验：只有管理员才能禁用用户
        User currentUser = this.getById(currentUserId);
        if (currentUser == null || currentUser.getIsAdmin() == null || currentUser.getIsAdmin() != 1) {
            throw new RuntimeException("权限不足，只有管理员才能禁用用户");
        }
        
        // 2. 不能禁用自己
        if (Objects.equals(userId, currentUserId)) {
            throw new RuntimeException("不能禁用自己的账号");
        }
        
        // 3. 不能禁用其他管理员
        User targetUser = this.getById(userId);
        if (targetUser == null || targetUser.getDeleted() == 1) {
            throw new RuntimeException("用户不存在或已被删除");
        }
        
        if (targetUser.getIsAdmin() != null && targetUser.getIsAdmin() == 1) {
            throw new RuntimeException("不能禁用其他管理员账号");
        }
        
        // 4. 执行禁用操作（设置status字段为0）
        targetUser.setStatus(0);
        return this.updateById(targetUser);
    }
}