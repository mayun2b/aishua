package zysy.iflytek.aishua.modules.ai.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.PageResult;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.ai.entity.vo.LearningAnalysisReportVO;
import zysy.iflytek.aishua.modules.ai.service.LearningAnalysisReportService;

@RestController
@RequestMapping("/api/ai/learning-analysis")
public class LearningAnalysisController {
    private final LearningAnalysisReportService learningAnalysisReportService;

    public LearningAnalysisController(LearningAnalysisReportService learningAnalysisReportService) {
        this.learningAnalysisReportService = learningAnalysisReportService;
    }

    @GetMapping
    public Result<PageResult<LearningAnalysisReportVO>> listHistory(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于 0") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于 0")
            @Max(value = 100, message = "每页数量不能超过 100") Integer pageSize
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(learningAnalysisReportService.listHistory(userId, subjectId, pageNum, pageSize));
    }

    @GetMapping("/{reportId}")
    public Result<LearningAnalysisReportVO> getById(@PathVariable Long reportId) {
        Long userId = UserContext.requireUserId();
        return Result.success(learningAnalysisReportService.getById(userId, reportId));
    }

    @GetMapping("/latest")
    public Result<LearningAnalysisReportVO> getLatest(@RequestParam(required = false) Long subjectId) {
        Long userId = UserContext.requireUserId();
        return Result.success(learningAnalysisReportService.getLatest(userId, subjectId));
    }
}
