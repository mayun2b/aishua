package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 练习视图对象，用于接口出参封装。
 */
@Data
public class PracticeSessionDetailVO {
    private Long sessionId;

    private Long subjectId;

    private String subjectName;

    private Integer practiceMode;

    private Integer questionCount;

    private Integer answeredCount;

    private Integer correctCount;

    private Integer wrongCount;

    private Integer totalTimeCost;

    private Integer draftVersion;

    private Integer status;

    private BigDecimal correctRate;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private List<PracticeExerciseRecordVO> records = new ArrayList<>();
}
