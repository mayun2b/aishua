package zysy.iflytek.aishua.modules.user.support;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 用户密码编解码组件，兼容新旧两种密码算法。
 */
@Component
public class PasswordCodec {
    private static final String LEGACY_PREFIX = "aishua";
    private static final String LEGACY_SUFFIX = "2024";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 使用 BCrypt 对明文密码进行加密。
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 校验密码是否匹配：优先按 BCrypt 校验，否则按历史 MD5 规则校验。
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        if (isBcrypt(encodedPassword)) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
        return legacyEncode(rawPassword).equals(encodedPassword);
    }

    /**
     * 判断历史口令是否需要升级为 BCrypt。
     */
    public boolean needsUpgrade(String encodedPassword) {
        return encodedPassword != null && !encodedPassword.isBlank() && !isBcrypt(encodedPassword);
    }

    /**
     * 判断密文是否为 BCrypt 结果。
     */
    private boolean isBcrypt(String encodedPassword) {
        return encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$");
    }

    /**
     * 历史口令算法：前后拼接固定盐值后做 MD5。
     */
    private String legacyEncode(String rawPassword) {
        return DigestUtils.md5DigestAsHex((LEGACY_PREFIX + rawPassword + LEGACY_SUFFIX).getBytes(StandardCharsets.UTF_8));
    }
}
