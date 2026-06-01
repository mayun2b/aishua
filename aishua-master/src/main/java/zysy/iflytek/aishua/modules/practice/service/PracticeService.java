package zysy.iflytek.aishua.modules.practice.service;

import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeBatchSubmitDTO;
import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeDraftSaveDTO;
import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeStartDTO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeBatchSubmitResultVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeDraftSnapshotVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeExerciseRecordVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeQuestionSheetVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeSessionDetailVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeSessionSummaryVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeStatsVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeStartVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeWrongTrendVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeWrongQuestionVO;
import zysy.iflytek.aishua.modules.tag.entity.vo.ExamTagVO;

import java.util.List;

/**
 * 练习服务接口，定义该领域对外能力契约。
 */
public interface PracticeService {
    /**
     * 创建新的练习会话并生成题目清单。
     */
    PracticeStartVO startPractice(Long userId, PracticeStartDTO practiceStartDTO);

    /**
     * 获取练习会话题目列表（不含判分结果）。
     */
    PracticeQuestionSheetVO getPracticeQuestions(Long userId, Long sessionId);

    /**
     * 获取练习草稿快照（优先缓存，必要时回退数据库）。
     */
    PracticeDraftSnapshotVO getPracticeDraft(Long userId, Long sessionId);

    /**
     * 增量保存练习草稿，带版本校验避免并发覆盖。
     */
    PracticeDraftSnapshotVO savePracticeDraft(Long userId, Long sessionId, PracticeDraftSaveDTO practiceDraftSaveDTO);

    /**
     * 提交练习并返回批量判题结果。
     */
    PracticeBatchSubmitResultVO submitPractice(Long userId, Long sessionId, PracticeBatchSubmitDTO practiceBatchSubmitDTO);

    /**
     * 查询练习会话列表，可按学科筛选。
     */
    List<PracticeSessionSummaryVO> listPracticeSessions(Long userId, Long subjectId);

    /**
     * 查询单个练习会话明细。
     */
    PracticeSessionDetailVO getPracticeSessionDetail(Long userId, Long sessionId);

    /**
     * 查询做题记录（含题目与作答信息）。
     */
    List<PracticeExerciseRecordVO> listExerciseRecords(Long userId, Long subjectId);

    /**
     * 查询错题列表，支持按目录和掌握状态筛选。
     */
    List<PracticeWrongQuestionVO> listWrongQuestions(Long userId, Long subjectId, Long directoryId, Integer masterStatus);

    /**
     * 更新错题掌握状态（未掌握/已掌握）。
     */
    PracticeWrongQuestionVO updateWrongQuestionMasterStatus(Long userId, Long wrongQuestionId, Integer masterStatus);

    /**
     * 查询错题趋势统计。
     */
    List<PracticeWrongTrendVO> getWrongQuestionTrends(Long userId, Long subjectId, Long directoryId, Integer days);

    /**
     * 查询练习统计看板数据。
     */
    PracticeStatsVO getPracticeStats(Long userId, Long subjectId, Integer days);

    /**
     * 查询某学科下可用于练习筛选的知识点标签。
     */
    List<ExamTagVO> listPracticeTags(Long userId, Long subjectId);
}
