package zysy.iflytek.aishua.modules.ai.entity.dto;

import lombok.Data;

/**
 * 智能问答数据传输对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeQuestionAiCreateSessionDTO {
    /**
     * 1-手动创建，2-自动提醒创建
     */
    private Integer triggerSource;
}
