package zysy.iflytek.aishua.modules.ai.entity.dto;

import lombok.Data;

@Data
public class PracticeQuestionAiCreateSessionDTO {
    /**
     * 1-manual, 2-auto-reminder
     */
    private Integer triggerSource;
}
