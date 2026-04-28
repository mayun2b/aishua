package zysy.iflytek.aishua.modules.user.service;

import zysy.iflytek.aishua.modules.user.entity.dto.LoginDTO;
import zysy.iflytek.aishua.modules.user.entity.dto.RegisterDTO;
import zysy.iflytek.aishua.modules.user.entity.vo.LoginVO;
import zysy.iflytek.aishua.modules.user.entity.vo.UserProfileVO;

public interface UserService {
    LoginVO login(LoginDTO loginDTO);

    UserProfileVO register(RegisterDTO registerDTO);

    UserProfileVO getProfile(Long userId);
}
