package zysy.iflytek.aishua.modules.practice.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.Result;
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
import zysy.iflytek.aishua.modules.practice.service.PracticeService;
import zysy.iflytek.aishua.modules.tag.entity.vo.ExamTagVO;

import java.util.List;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {
    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @PostMapping("/start")
    public Result<PracticeStartVO> startPractice(@Valid @RequestBody PracticeStartDTO practiceStartDTO) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.startPractice(userId, practiceStartDTO));
    }

    @GetMapping("/{sessionId}/questions")
    public Result<PracticeQuestionSheetVO> getPracticeQuestions(@PathVariable Long sessionId) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.getPracticeQuestions(userId, sessionId));
    }

    @GetMapping("/sessions")
    public Result<List<PracticeSessionSummaryVO>> listPracticeSessions(
            @RequestParam(required = false) Long subjectId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.listPracticeSessions(userId, subjectId));
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<PracticeSessionDetailVO> getPracticeSessionDetail(@PathVariable Long sessionId) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.getPracticeSessionDetail(userId, sessionId));
    }

    @GetMapping("/records")
    public Result<List<PracticeExerciseRecordVO>> listExerciseRecords(
            @RequestParam(required = false) Long subjectId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.listExerciseRecords(userId, subjectId));
    }

    @GetMapping("/wrong-questions")
    public Result<List<PracticeWrongQuestionVO>> listWrongQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long directoryId,
            @RequestParam(required = false) Integer masterStatus
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.listWrongQuestions(userId, subjectId, directoryId, masterStatus));
    }

    @PutMapping("/wrong-questions/{wrongQuestionId}/master-status")
    public Result<PracticeWrongQuestionVO> updateWrongQuestionMasterStatus(
            @PathVariable Long wrongQuestionId,
            @RequestParam Integer masterStatus
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.updateWrongQuestionMasterStatus(userId, wrongQuestionId, masterStatus));
    }

    @GetMapping("/wrong-questions/trend")
    public Result<List<PracticeWrongTrendVO>> getWrongQuestionTrends(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long directoryId,
            @RequestParam(required = false) Integer days
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.getWrongQuestionTrends(userId, subjectId, directoryId, days));
    }

    @GetMapping("/stats")
    public Result<PracticeStatsVO> getPracticeStats(
            @RequestParam(required = false) Integer days
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.getPracticeStats(userId, days));
    }

    @GetMapping("/tags")
    public Result<List<ExamTagVO>> listPracticeTags(
            @RequestParam Long subjectId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.listPracticeTags(userId, subjectId));
    }

    @PostMapping("/{sessionId}/submit-all")
    public Result<PracticeBatchSubmitResultVO> submitPractice(
            @PathVariable Long sessionId,
            @Valid @RequestBody PracticeBatchSubmitDTO practiceBatchSubmitDTO
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceService.submitPractice(userId, sessionId, practiceBatchSubmitDTO));
    }

    @PutMapping("/{sessionId}/draft")
    public Result<Boolean> savePracticeDraft(
            @PathVariable Long sessionId,
            @Valid @RequestBody PracticeBatchSubmitDTO practiceBatchSubmitDTO
    ) {
        Long userId = UserContext.requireUserId();
        practiceService.savePracticeDraft(userId, sessionId, practiceBatchSubmitDTO);
        return Result.success(true);
    }
}
