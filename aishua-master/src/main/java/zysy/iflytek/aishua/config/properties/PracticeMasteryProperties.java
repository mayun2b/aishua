package zysy.iflytek.aishua.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统配置配置属性，负责相关业务逻辑与流程处理。
 */
@Data
@ConfigurationProperties(prefix = "practice.mastery")
public class PracticeMasteryProperties {
    /**
     * 样本量达到该阈值后，按正确率稳定分级。
     */
    private Integer minSampleCount = 20;

    /**
     * 掌握等级1的正确率阈值（百分比）。
     */
    private Integer rateLevel1 = 60;

    /**
     * 掌握等级2的正确率阈值（百分比）。
     */
    private Integer rateLevel2 = 75;

    /**
     * 掌握等级3的正确率阈值（百分比）。
     */
    private Integer rateLevel3 = 90;

    /**
     * 小样本阶段的最高等级限制。
     */
    private Integer warmupLevelCap = 2;
}
