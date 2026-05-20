package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ExamAvailableQuestionPageVO {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<ExamAvailableQuestionVO> records = Collections.emptyList();
}

