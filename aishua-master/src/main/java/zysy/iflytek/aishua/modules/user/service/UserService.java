package zysy.iflytek.aishua.modules.user.service;

import zysy.iflytek.aishua.modules.user.entity.dto.LoginDTO;
import zysy.iflytek.aishua.modules.user.entity.dto.RegisterDTO;
import zysy.iflytek.aishua.modules.user.entity.vo.LoginVO;
import zysy.iflytek.aishua.modules.user.entity.vo.UserProfileVO;

/**
 * 用户服务接口，定义该领域对外能力契约。
 */
public interface UserService {
    LoginVO login(LoginDTO loginDTO);

    UserProfileVO register(RegisterDTO registerDTO);

    UserProfileVO getProfile(Long userId);
}
