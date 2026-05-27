package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 练习视图对象，负责相关业务逻辑与流程处理。
 */
@Data
public class PracticeDraftSnapshotVO {
    private Long sessionId;

    private Integer version;

    private Integer answeredCount;

    private Integer totalTimeCost;

    private Long savedAt;

    private List<PracticeDraftAnswerVO> answers = new ArrayList<>();
}
