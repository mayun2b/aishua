package zysy.iflytek.aishua.modules.exam.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import zysy.iflytek.aishua.common.security.AdminAccess;
import zysy.iflytek.aishua.modules.exam.entity.dto.AdminExamPaperQuestionAssignDTO;
import zysy.iflytek.aishua.modules.exam.entity.dto.AdminExamPaperUpsertDTO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamAvailableQuestionPageVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamDirectoryTagVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamPaperQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamPaperVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordQuestionVO;
import zysy.iflytek.aishua.modules.exam.entity.vo.ExamRecordSummaryVO;
import zysy.iflytek.aishua.modules.exam.service.ExamService;
import zysy.iflytek.aishua.modules.directory.entity.vo.DirectoryTreeVO;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/exams")
public class AdminExamController {
    private final ExamService examService;
    private final AdminAccess adminAccess;

    public AdminExamController(ExamService examService, AdminAccess adminAccess) {
        this.examService = examService;
        this.adminAccess = adminAccess;
    }

    @GetMapping("/papers")
    public Result<List<ExamPaperVO>> listPapers(
            @RequestParam(required = false) @Min(value = 1, message = "学科ID不合法") Long subjectId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.listAdminPapers(subjectId, status, keyword));
    }

    @PostMapping("/papers")
    public Result<ExamPaperVO> createPaper(@Valid @RequestBody AdminExamPaperUpsertDTO upsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.createPaper(upsertDTO));
    }

    @PutMapping("/papers/{id}")
    public Result<ExamPaperVO> updatePaper(
            @PathVariable Long id,
            @Valid @RequestBody AdminExamPaperUpsertDTO upsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.updatePaper(id, upsertDTO));
    }

    @PutMapping("/papers/{id}/enabled")
    public Result<ExamPaperVO> updatePaperEnabled(
            @PathVariable Long id,
            @RequestParam Integer enabled
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.updatePaperEnabled(id, enabled));
    }

    @DeleteMapping("/papers/{id}")
    public Result<Void> deletePaper(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        examService.deletePaper(id);
        return Result.success();
    }

    @GetMapping("/papers/{id}/questions")
    public Result<List<ExamPaperQuestionVO>> listPaperQuestions(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.listPaperQuestions(id));
    }

    @GetMapping("/papers/{id}/directories")
    public Result<List<DirectoryTreeVO>> listPaperDirectories(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.listPaperDirectories(id));
    }

    @GetMapping("/papers/{id}/directories/{directoryId}/tags")
    public Result<List<ExamDirectoryTagVO>> listPaperDirectoryTags(
            @PathVariable Long id,
            @PathVariable @Min(value = 1, message = "目录ID不合法") Long directoryId
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.listPaperDirectoryTags(id, directoryId));
    }

    @GetMapping("/papers/{id}/questions/available")
    public Result<ExamAvailableQuestionPageVO> listPaperAvailableQuestions(
            @PathVariable Long id,
            @RequestParam(required = false) @Min(value = 1, message = "目录ID不合法") Long directoryId,
            @RequestParam(required = false) String tagIds,
            @RequestParam(required = false) @Min(value = 1, message = "题型范围为 1-5")
            @Max(value = 5, message = "题型范围为 1-5") Integer type,
            @RequestParam(required = false) @Min(value = 1, message = "难度范围为 1-3")
            @Max(value = 3, message = "难度范围为 1-3") Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于 0") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于 0")
            @Max(value = 100, message = "每页数量不能超过 100") Integer pageSize
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.listPaperAvailableQuestions(
                id,
                directoryId,
                tagIds,
                type,
                difficulty,
                keyword,
                page,
                pageSize
        ));
    }

    @PutMapping("/papers/{id}/questions")
    public Result<List<ExamPaperQuestionVO>> assignPaperQuestions(
            @PathVariable Long id,
            @Valid @RequestBody AdminExamPaperQuestionAssignDTO assignDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.assignPaperQuestions(id, assignDTO));
    }

    @GetMapping("/records")
    public Result<List<ExamRecordSummaryVO>> listRecords(
            @RequestParam(required = false) @Min(value = 1, message = "学科ID不合法") Long subjectId,
            @RequestParam(required = false) @Min(value = 1, message = "用户ID不合法") Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.listAdminRecords(subjectId, userId, keyword, startDate, endDate));
    }

    @GetMapping("/records/{id}")
    public Result<ExamRecordSummaryVO> recordDetail(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.getAdminRecord(id));
    }

    @GetMapping("/records/{id}/questions")
    public Result<List<ExamRecordQuestionVO>> recordQuestions(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        return Result.success(examService.getAdminRecordQuestions(id));
    }
}
