package zysy.iflytek.aishua.modules.user.support;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 用户支撑组件，负责相关业务逻辑与流程处理。
 */
@Component
public class PasswordCodec {
    private static final String LEGACY_PREFIX = "aishua";
    private static final String LEGACY_SUFFIX = "2024";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 提供通用支撑处理能力。
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 提供通用支撑处理能力。
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
     * 提供通用支撑处理能力。
     */
    public boolean needsUpgrade(String encodedPassword) {
        return encodedPassword != null && !encodedPassword.isBlank() && !isBcrypt(encodedPassword);
    }

    /**
     * 判断支撑条件是否满足。
     */
    private boolean isBcrypt(String encodedPassword) {
        return encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$");
    }

    /**
     * 提供通用支撑处理能力。
     */
    private String legacyEncode(String rawPassword) {
        return DigestUtils.md5DigestAsHex((LEGACY_PREFIX + rawPassword + LEGACY_SUFFIX).getBytes(StandardCharsets.UTF_8));
    }
}
