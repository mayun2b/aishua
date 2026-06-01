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

/**
 * 考试控制器，提供该领域对外接口入口。
 */
@Validated
@RestController
@RequestMapping("/api/admin/exams")
public class AdminExamController {
    private final ExamService examService;
    private final AdminAccess adminAccess;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public AdminExamController(ExamService examService, AdminAccess adminAccess) {
        this.examService = examService;
        this.adminAccess = adminAccess;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/papers")
    public Result<List<ExamPaperVO>> listPapers(
            @RequestParam(required = false) @Min(value = 1, message = "学科编号不合法") Long subjectId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listAdminPapers(subjectId, status, keyword));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping("/papers")
    public Result<ExamPaperVO> createPaper(@Valid @RequestBody AdminExamPaperUpsertDTO upsertDTO) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.createPaper(upsertDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/papers/{id}")
    public Result<ExamPaperVO> updatePaper(
            @PathVariable Long id,
            @Valid @RequestBody AdminExamPaperUpsertDTO upsertDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.updatePaper(id, upsertDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/papers/{id}/enabled")
    public Result<ExamPaperVO> updatePaperEnabled(
            @PathVariable Long id,
            @RequestParam Integer enabled
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.updatePaperEnabled(id, enabled));
    }

    /**
     * 删除接口入口，负责资源删除与结果返回。
     */
    @DeleteMapping("/papers/{id}")
    public Result<Void> deletePaper(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        examService.deletePaper(id);
        // 调用服务层处理业务并封装统一响应。
        return Result.success();
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/papers/{id}/questions")
    public Result<List<ExamPaperQuestionVO>> listPaperQuestions(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listPaperQuestions(id));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/papers/{id}/directories")
    public Result<List<DirectoryTreeVO>> listPaperDirectories(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listPaperDirectories(id));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/papers/{id}/directories/{directoryId}/tags")
    public Result<List<ExamDirectoryTagVO>> listPaperDirectoryTags(
            @PathVariable Long id,
            @PathVariable @Min(value = 1, message = "目录编号不合法") Long directoryId
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listPaperDirectoryTags(id, directoryId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/papers/{id}/questions/available")
    public Result<ExamAvailableQuestionPageVO> listPaperAvailableQuestions(
            @PathVariable Long id,
            @RequestParam(required = false) @Min(value = 1, message = "目录编号不合法") Long directoryId,
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
        // 调用服务层处理业务并封装统一响应。
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

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PutMapping("/papers/{id}/questions")
    public Result<List<ExamPaperQuestionVO>> assignPaperQuestions(
            @PathVariable Long id,
            @Valid @RequestBody AdminExamPaperQuestionAssignDTO assignDTO
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.assignPaperQuestions(id, assignDTO));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records")
    public Result<List<ExamRecordSummaryVO>> listRecords(
            @RequestParam(required = false) @Min(value = 1, message = "学科编号不合法") Long subjectId,
            @RequestParam(required = false) @Min(value = 1, message = "用户编号不合法") Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listAdminRecords(subjectId, userId, keyword, startDate, endDate));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @GetMapping("/records/{id}")
    public Result<ExamRecordSummaryVO> recordDetail(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.getAdminRecord(id));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @GetMapping("/records/{id}/questions")
    public Result<List<ExamRecordQuestionVO>> recordQuestions(@PathVariable Long id) {
        adminAccess.ensureAdmin(UserContext.requireUserId());
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.getAdminRecordQuestions(id));
    }
}
