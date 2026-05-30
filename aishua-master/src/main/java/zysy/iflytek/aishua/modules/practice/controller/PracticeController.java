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
import zysy.iflytek.aishua.modules.practice.service.PracticeService;
import zysy.iflytek.aishua.modules.tag.entity.vo.ExamTagVO;

import java.util.List;

/**
 * 练习控制器，负责相关业务逻辑与流程处理。
 */
@RestController
@RequestMapping("/api/practice")
public class PracticeController {
    private final PracticeService practiceService;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping("/start")
    public Result<PracticeStartVO> startPractice(@Valid @RequestBody PracticeStartDTO practiceStartDTO) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.startPractice(userId, practiceStartDTO));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/{sessionId}/questions")
    public Result<PracticeQuestionSheetVO> getPracticeQuestions(@PathVariable Long sessionId) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.getPracticeQuestions(userId, sessionId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/sessions")
    public Result<List<PracticeSessionSummaryVO>> listPracticeSessions(
            @RequestParam(required = false) Long subjectId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.listPracticeSessions(userId, subjectId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/sessions/{sessionId}")
    public Result<PracticeSessionDetailVO> getPracticeSessionDetail(@PathVariable Long sessionId) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.getPracticeSessionDetail(userId, sessionId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/records")
    public Result<List<PracticeExerciseRecordVO>> listExerciseRecords(
            @RequestParam(required = false) Long subjectId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.listExerciseRecords(userId, subjectId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/wrong-questions")
    public Result<List<PracticeWrongQuestionVO>> listWrongQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long directoryId,
            @RequestParam(required = false) Integer masterStatus
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.listWrongQuestions(userId, subjectId, directoryId, masterStatus));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/wrong-questions/{wrongQuestionId}/master-status")
    public Result<PracticeWrongQuestionVO> updateWrongQuestionMasterStatus(
            @PathVariable Long wrongQuestionId,
            @RequestParam Integer masterStatus
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.updateWrongQuestionMasterStatus(userId, wrongQuestionId, masterStatus));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/wrong-questions/trend")
    public Result<List<PracticeWrongTrendVO>> getWrongQuestionTrends(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long directoryId,
            @RequestParam(required = false) Integer days
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.getWrongQuestionTrends(userId, subjectId, directoryId, days));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/stats")
    public Result<PracticeStatsVO> getPracticeStats(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer days
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.getPracticeStats(userId, subjectId, days));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/tags")
    public Result<List<ExamTagVO>> listPracticeTags(
            @RequestParam Long subjectId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.listPracticeTags(userId, subjectId));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PostMapping("/{sessionId}/submit-all")
    public Result<PracticeBatchSubmitResultVO> submitPractice(
            @PathVariable Long sessionId,
            @Valid @RequestBody PracticeBatchSubmitDTO practiceBatchSubmitDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.submitPractice(userId, sessionId, practiceBatchSubmitDTO));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/{sessionId}/draft")
    public Result<PracticeDraftSnapshotVO> getPracticeDraft(
            @PathVariable Long sessionId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.getPracticeDraft(userId, sessionId));
    }

    /**
     * 处理更新请求并返回结果。
     */
    @PutMapping("/{sessionId}/draft")
    public Result<PracticeDraftSnapshotVO> savePracticeDraft(
            @PathVariable Long sessionId,
            @Valid @RequestBody PracticeDraftSaveDTO practiceDraftSaveDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceService.savePracticeDraft(userId, sessionId, practiceDraftSaveDTO));
    }
}
