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
     * 构造方法，注入当前类所需依赖。
     */
    private AiServiceSupport() {
    }

    /**
     * 空值数字兜底，避免空指针分支散落在业务代码中。
     */
    public static Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 生成去横杠的随机编码，可用于会话号或消息号。
     */
    public static String newCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 将本地角色枚举映射为模型接口角色名。
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
     * 将本地角色枚举映射为可读角色名（兜底返回 unknown）。
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
