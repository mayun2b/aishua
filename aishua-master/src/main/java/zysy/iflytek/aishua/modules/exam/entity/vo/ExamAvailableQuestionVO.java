package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.util.List;

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

