package zysy.iflytek.aishua.modules.ai.support;

import java.util.UUID;

/**
 * 智能服务公共工具：角色映射、默认值处理与通用编码生成。
 */
public final class AiServiceSupport {
    public static final int ROLE_SYSTEM = 1;
    public static final int ROLE_USER = 2;
    public static final int ROLE_ASSISTANT = 3;

    /**
     * 构造方法，负责注入依赖组件。
     */
    private AiServiceSupport() {
    }

    /**
     * 提供通用支撑处理能力。
     */
    public static Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 提供通用支撑处理能力。
     */
    public static String newCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 提供通用支撑处理能力。
     */
    public static String mapRoleToOpenAiRole(Integer role) {
        if (ROLE_SYSTEM == role) {
            return "system";
        }
        if (ROLE_USER == role) {
            return "user";
        }
        if (ROLE_ASSISTANT == role) {
            return "assistant";
        }
        return null;
    }

    /**
     * 提供通用支撑处理能力。
     */
    public static String mapRoleName(Integer role) {
        if (ROLE_SYSTEM == role) {
            return "system";
        }
        if (ROLE_USER == role) {
            return "user";
        }
        if (ROLE_ASSISTANT == role) {
            return "assistant";
        }
        return "unknown";
    }
}
