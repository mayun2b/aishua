package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PracticeBatchSubmitResultVO {
    private Long sessionId;

    private Integer questionCount;

    private Integer answeredCount;

    private Integer correctCount;

    private Integer wrongCount;

    private Integer totalTimeCost;

    private BigDecimal correctRate;

    private Boolean finished;

    private List<PracticeAnswerResultVO> results = new ArrayList<>();
}
