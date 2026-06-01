package zysy.iflytek.aishua.modules.ai.support;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.config.properties.QwenAiProperties;

/**
 * 统一解析通义千问会话参数，避免服务层重复逻辑。
 */
@Component
public class QwenChatOptionsResolver {
    private static final String MODEL_PROVIDER = "qwen";
    private static final String DEFAULT_CHAT_MODEL = "qwen3.6-plus";
    private static final int DEFAULT_CHAT_MAX_TOKENS = 220;

    private final QwenAiProperties qwenAiProperties;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public QwenChatOptionsResolver(QwenAiProperties qwenAiProperties) {
        this.qwenAiProperties = qwenAiProperties;
    }

    /**
     * 用于持久化与审计的提供方标识。
     */
    public String modelProvider() {
        return MODEL_PROVIDER;
    }

    /**
     * 实际生效的模型名称，配置为空时回退默认值。
     */
    public String resolveChatModelName() {
        if (StringUtils.hasText(qwenAiProperties.getChatModel())) {
            return qwenAiProperties.getChatModel().trim();
        }
        return DEFAULT_CHAT_MODEL;
    }

    /**
     * 实际生效的最大令牌数，带上下界保护。
     */
    public int resolveBoundedChatMaxTokens(int minTokens, int maxTokens) {
        int configured = resolveConfiguredChatMaxTokens();
        int safeMin = Math.max(minTokens, 1);
        int safeMax = Math.max(maxTokens, safeMin);
        return Math.min(Math.max(configured, safeMin), safeMax);
    }

    /**
     * 识别用户是否明确要求更详细的回答。
     */
    public boolean shouldAllowDetailedAnswer(String currentUserQuestion) {
        if (!StringUtils.hasText(currentUserQuestion)) {
            return false;
        }
        String text = currentUserQuestion.trim().toLowerCase();
        return text.contains("详细")
                || text.contains("展开")
                || text.contains("举例")
                || text.contains("detail")
                || text.contains("example");
    }

    /**
     * 读取配置中的最大令牌数，未配置时使用默认值。
     */
    private int resolveConfiguredChatMaxTokens() {
        return qwenAiProperties.getChatMaxTokens() == null
                ? DEFAULT_CHAT_MAX_TOKENS
                : qwenAiProperties.getChatMaxTokens();
    }
}
