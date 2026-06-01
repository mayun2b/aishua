package zysy.iflytek.aishua.modules.ai.entity.dto;

import lombok.Data;

/**
 * 智能问答数据传输对象，用于接口入参封装。
 */
@Data
public class PracticeQuestionAiCreateSessionDTO {
    /**
     * 1-手动创建，2-自动提醒创建
     */
    private Integer triggerSource;
}
