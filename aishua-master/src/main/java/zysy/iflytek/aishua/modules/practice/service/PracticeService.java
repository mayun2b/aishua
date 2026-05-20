package zysy.iflytek.aishua.modules.practice.service;

import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeBatchSubmitDTO;
import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeStartDTO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeBatchSubmitResultVO;
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

public interface PracticeService {
    PracticeStartVO startPractice(Long userId, PracticeStartDTO practiceStartDTO);

    PracticeQuestionSheetVO getPracticeQuestions(Long userId, Long sessionId);

    PracticeBatchSubmitResultVO submitPractice(Long userId, Long sessionId, PracticeBatchSubmitDTO practiceBatchSubmitDTO);

    List<PracticeSessionSummaryVO> listPracticeSessions(Long userId, Long subjectId);

    PracticeSessionDetailVO getPracticeSessionDetail(Long userId, Long sessionId);

    List<PracticeExerciseRecordVO> listExerciseRecords(Long userId, Long subjectId);

    List<PracticeWrongQuestionVO> listWrongQuestions(Long userId, Long subjectId, Long directoryId, Integer masterStatus);

    PracticeWrongQuestionVO updateWrongQuestionMasterStatus(Long userId, Long wrongQuestionId, Integer masterStatus);

    List<PracticeWrongTrendVO> getWrongQuestionTrends(Long userId, Long subjectId, Long directoryId, Integer days);

    PracticeStatsVO getPracticeStats(Long userId, Integer days);

    List<ExamTagVO> listPracticeTags(Long userId, Long subjectId);
}
