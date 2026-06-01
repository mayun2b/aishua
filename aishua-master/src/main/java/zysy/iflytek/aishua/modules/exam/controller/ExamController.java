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

/**
 * 考试控制器，提供该领域对外接口入口。
 */
@Validated
@RestController
@RequestMapping("/api/exam")
public class ExamController {
    private final ExamService examService;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/papers")
    public Result<List<ExamPaperVO>> listAvailablePapers(
            @RequestParam(required = false) @Min(value = 1, message = "学科编号不合法") Long subjectId
    ) {
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listAvailablePapers(subjectId));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping("/start")
    public Result<ExamStartVO> startExam(@Valid @RequestBody ExamStartDTO startDTO) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.startExam(userId, startDTO));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PostMapping("/{recordId}/submit")
    public Result<ExamSubmitResultVO> submitExam(
            @PathVariable Long recordId,
            @Valid @RequestBody ExamSubmitDTO submitDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.submitExam(userId, recordId, submitDTO));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records/me")
    public Result<List<ExamRecordSummaryVO>> listMyRecords() {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listMyRecords(userId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records/{id}")
    public Result<ExamRecordSummaryVO> getMyRecord(@PathVariable Long id) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.getMyRecord(userId, id));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records/{id}/questions")
    public Result<List<ExamRecordQuestionVO>> listMyRecordQuestions(@PathVariable Long id) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listMyRecordQuestions(userId, id));
    }

    // 兼容旧版接口调用。
    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records/user/{userId}")
    public Result<List<ExamRecordSummaryVO>> getExamRecordsByUser(@PathVariable Long userId) {
        // 从用户上下文获取当前登录用户编号。
        Long currentUserId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listUserRecordsByUserId(currentUserId, userId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records/user/{userId}/mode/{mode}")
    public Result<List<ExamRecordSummaryVO>> getExamRecordsByMode(
            @PathVariable Long userId,
            @PathVariable Integer mode
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long currentUserId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listUserRecordsByMode(currentUserId, userId, mode));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records/user/{userId}/date")
    public Result<List<ExamRecordSummaryVO>> getExamRecordsByDateRange(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long currentUserId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.listUserRecordsByDateRange(currentUserId, userId, startDate, endDate));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PostMapping("/records/save")
    public Result<ExamRecordSummaryVO> saveLegacyRecord(@Valid @RequestBody LegacyExamRecordSaveDTO saveDTO) {
        // 从用户上下文获取当前登录用户编号。
        Long currentUserId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(examService.saveLegacyRecord(currentUserId, saveDTO));
    }
}
