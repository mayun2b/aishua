package zysy.iflytek.aishua.modules.exam.entity.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 考试视图对象，用于接口出参封装。
 */
@Data
public class ExamAvailableQuestionPageVO {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<ExamAvailableQuestionVO> records = Collections.emptyList();
}

