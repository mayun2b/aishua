package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 练习草稿缓存与落库配置。
 */
@Data
@ConfigurationProperties(prefix = "practice.draft")
public class PracticeDraftProperties {
    /**
     * Redis 草稿键前缀。
     */
    private String redisKeyPrefix = "practice:draft:v2:";

    /**
     * 草稿缓存保留天数。
     */
    private int cacheTtlDays = 7;

    /**
     * 草稿保存锁有效秒数。
     */
    private int saveLockTtlSeconds = 5;

    /**
     * 单次草稿落库处理数量。
     */
    private int syncBatchSize = 50;

    /**
     * 草稿定时落库间隔（毫秒）。
     */
    private long dbSyncIntervalMs = 300000L;

    /**
     * 应用启动后首次草稿落库延迟（毫秒）。
     */
    private long dbSyncInitialDelayMs = 300000L;
}
