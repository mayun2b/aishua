package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeQuestionSheetVO {
    private Long sessionId;

    private Integer questionCount;

    private Integer answeredCount;

    private Boolean finished;

    private List<PracticeQuestionItemVO> questions = new ArrayList<>();
}
