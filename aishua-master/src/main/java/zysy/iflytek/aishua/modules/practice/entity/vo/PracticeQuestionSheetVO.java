package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 练习视图对象，用于接口出参封装。
 */
@Data
public class PracticeQuestionSheetVO {
    private Long sessionId;

    private Integer questionCount;

    private Integer answeredCount;

    private Boolean finished;

    private List<PracticeQuestionItemVO> questions = new ArrayList<>();
}
