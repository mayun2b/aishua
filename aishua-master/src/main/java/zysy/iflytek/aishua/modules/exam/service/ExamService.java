package zysy.iflytek.aishua.modules.exam.service;

import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.modules.exam.entity.dto.AdminExamPaperQuestionAssignDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.AdminExamPaperUpsertDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.ExamStartDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.ExamSubmitDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.LegacyExamRecordSaveDTO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamAvailableQuestionPageVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamDirectoryTagVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamPaperQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamPaperVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordSummaryVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamStartVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamSubmitResultVO;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 考试服务接口，定义该领域对外能力契约。
 */
public interface ExamService {
    List<ExamPaperVO> listAdminPapers(Long subjectId, Integer status, String keyword);

    ExamPaperVO createPaper(AdminExamPaperUpsertDTO upsertDTO);

    ExamPaperVO updatePaper(Long id, AdminExamPaperUpsertDTO upsertDTO);

    ExamPaperVO updatePaperEnabled(Long id, Integer enabled);

    void deletePaper(Long id);

    List<ExamPaperQuestionVO> listPaperQuestions(Long paperId);

    List<ExamPaperQuestionVO> assignPaperQuestions(Long paperId, AdminExamPaperQuestionAssignDTO assignDTO);

    List<DirectoryTreeVO> listPaperDirectories(Long paperId);

    List<ExamDirectoryTagVO> listPaperDirectoryTags(Long paperId, Long directoryId);

    ExamAvailableQuestionPageVO listPaperAvailableQuestions(
            Long paperId,
            Long directoryId,
            String tagIds,
            Integer type,
            Integer difficulty,
            String keyword,
            Integer page,
            Integer pageSize
    );

    PageResult<ExamRecordSummaryVO> listAdminRecords(
            Long subjectId,
            Long userId,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Integer pageNum,
            Integer pageSize
    );

    ExamRecordSummaryVO getAdminRecord(Long recordId);

    List<ExamRecordQuestionVO> getAdminRecordQuestions(Long recordId);

    List<ExamPaperVO> listAvailablePapers(Long subjectId);

    ExamStartVO startExam(Long userId, ExamStartDTO startDTO);

    ExamSubmitResultVO submitExam(Long userId, Long recordId, ExamSubmitDTO submitDTO);

    PageResult<ExamRecordSummaryVO> listMyRecords(Long userId, Long subjectId, Integer status, Integer pageNum, Integer pageSize);

    ExamRecordSummaryVO getMyRecord(Long userId, Long recordId);

    List<ExamRecordQuestionVO> listMyRecordQuestions(Long userId, Long recordId);

    List<ExamRecordSummaryVO> listUserRecordsByUserId(Long currentUserId, Long userId);

    List<ExamRecordSummaryVO> listUserRecordsByMode(Long currentUserId, Long userId, Integer mode);

    List<ExamRecordSummaryVO> listUserRecordsByDateRange(Long currentUserId, Long userId, LocalDate startDate, LocalDate endDate);

    ExamRecordSummaryVO saveLegacyRecord(Long currentUserId, LegacyExamRecordSaveDTO saveDTO);
}
