package zysy.iflytek.aishua.modules.exam.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.exam.entity.dto.ExamStartDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.ExamSubmitDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.LegacyExamRecordSaveDTO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamPaperVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordSummaryVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamStartVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamSubmitResultVO;
import zysy.iflytek.aishua.modules.exam.service.ExamService;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/exam")
public class ExamController {
    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/papers")
    public Result<List<ExamPaperVO>> listAvailablePapers(
            @RequestParam(required = false) @Min(value = 1, message = "学科ID不合法") Long subjectId
    ) {
        return Result.success(examService.listAvailablePapers(subjectId));
    }

    @PostMapping("/start")
    public Result<ExamStartVO> startExam(@Valid @RequestBody ExamStartDTO startDTO) {
        Long userId = UserContext.requireUserId();
        return Result.success(examService.startExam(userId, startDTO));
    }

    @PostMapping("/{recordId}/submit")
    public Result<ExamSubmitResultVO> submitExam(
            @PathVariable Long recordId,
            @Valid @RequestBody ExamSubmitDTO submitDTO
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(examService.submitExam(userId, recordId, submitDTO));
    }

    @GetMapping("/records/me")
    public Result<List<ExamRecordSummaryVO>> listMyRecords() {
        Long userId = UserContext.requireUserId();
        return Result.success(examService.listMyRecords(userId));
    }

    @GetMapping("/records/{id}")
    public Result<ExamRecordSummaryVO> getMyRecord(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        return Result.success(examService.getMyRecord(userId, id));
    }

    @GetMapping("/records/{id}/questions")
    public Result<List<ExamRecordQuestionVO>> listMyRecordQuestions(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        return Result.success(examService.listMyRecordQuestions(userId, id));
    }

    // legacy compatibility
    @GetMapping("/records/user/{userId}")
    public Result<List<ExamRecordSummaryVO>> getExamRecordsByUser(@PathVariable Long userId) {
        Long currentUserId = UserContext.requireUserId();
        return Result.success(examService.listUserRecordsByUserId(currentUserId, userId));
    }

    @GetMapping("/records/user/{userId}/mode/{mode}")
    public Result<List<ExamRecordSummaryVO>> getExamRecordsByMode(
            @PathVariable Long userId,
            @PathVariable Integer mode
    ) {
        Long currentUserId = UserContext.requireUserId();
        return Result.success(examService.listUserRecordsByMode(currentUserId, userId, mode));
    }

    @GetMapping("/records/user/{userId}/date")
    public Result<List<ExamRecordSummaryVO>> getExamRecordsByDateRange(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long currentUserId = UserContext.requireUserId();
        return Result.success(examService.listUserRecordsByDateRange(currentUserId, userId, startDate, endDate));
    }

    @PostMapping("/records/save")
    public Result<ExamRecordSummaryVO> saveLegacyRecord(@Valid @RequestBody LegacyExamRecordSaveDTO saveDTO) {
        Long currentUserId = UserContext.requireUserId();
        return Result.success(examService.saveLegacyRecord(currentUserId, saveDTO));
    }
}
