package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 考试视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class ExamAvailableQuestionPageVO {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<ExamAvailableQuestionVO> records = Collections.emptyList();
}

