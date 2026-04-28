package zysy.iflytek.aishua.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.common.security.JwtService;
import zysy.iflytek.aishua.modules.user.entity.User;
import zysy.iflytek.aishua.modules.user.entity.dto.LoginDTO;
import zysy.iflytek.aishua.modules.user.entity.dto.RegisterDTO;
import zysy.iflytek.aishua.modules.user.entity.vo.LoginVO;
import zysy.iflytek.aishua.modules.user.entity.vo.UserProfileVO;
import zysy.iflytek.aishua.modules.user.mapper.UserMapper;
import zysy.iflytek.aishua.modules.user.service.UserService;
import zysy.iflytek.aishua.modules.user.support.PasswordCodec;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordCodec passwordCodec;

    public UserServiceImpl(UserMapper userMapper, JwtService jwtService, PasswordCodec passwordCodec) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.passwordCodec = passwordCodec;
    }

    @Override
    @Transactional
    public LoginVO login(LoginDTO loginDTO) {
        User user = getByPhone(loginDTO.getPhone());
        if (user == null) {
            throw new BusinessException("手机号未注册", 401);
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用", 403);
        }
        if (!passwordCodec.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误", 401);
        }

        // 登录成功后顺手升级旧版密码哈希，避免一次性迁移风险。
        if (passwordCodec.needsUpgrade(user.getPassword())) {
            user.setPassword(passwordCodec.encode(loginDTO.getPassword()));
            userMapper.updateById(user);
        }

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(jwtService.generateToken(user.getId()));
        loginVO.setUser(toLoginUser(user));
        log.info("用户登录成功，userId={}", user.getId());
        return loginVO;
    }

    @Override
    @Transactional
    public UserProfileVO register(RegisterDTO registerDTO) {
        if (getByPhone(registerDTO.getPhone()) != null) {
            throw new BusinessException("该手机号已注册", 400);
        }

        User user = new User();
        user.setPhone(registerDTO.getPhone());
        user.setPassword(passwordCodec.encode(registerDTO.getPassword()));
        user.setNickname(resolveNickname(registerDTO));
        user.setStatus(1);
        user.setIsAdmin(0);
        userMapper.insert(user);

        log.info("用户注册成功，userId={}", user.getId());
        return toProfile(user);
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在", 404);
        }
        return toProfile(user);
    }

    private User getByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getDeleted, 0)
                .last("limit 1"));
    }

    private String resolveNickname(RegisterDTO registerDTO) {
        String nickname = registerDTO.getNickname();
        if (nickname != null && !nickname.isBlank()) {
            return nickname.trim();
        }
        String phone = registerDTO.getPhone();
        return "用户" + phone.substring(phone.length() - 4);
    }

    private LoginVO.UserVO toLoginUser(User user) {
        LoginVO.UserVO userVO = new LoginVO.UserVO();
        userVO.setId(user.getId());
        userVO.setPhone(user.getPhone());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setIsAdmin(user.getIsAdmin());
        return userVO;
    }

    private UserProfileVO toProfile(User user) {
        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setId(user.getId());
        profileVO.setPhone(user.getPhone());
        profileVO.setNickname(user.getNickname());
        profileVO.setAvatar(user.getAvatar());
        profileVO.setIsAdmin(user.getIsAdmin());
        return profileVO;
    }
}
