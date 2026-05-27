package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 考试视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class ExamAvailableQuestionVO {
    private Long id;
    private String title;
    private Integer type;
    private Integer difficulty;
    private Long directoryId;
    private String directoryName;
    private List<Long> tagIds;
    private Boolean selected;
}

