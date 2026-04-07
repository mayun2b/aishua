package zysy.iflytek.aishuai.stats.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;
import zysy.iflytek.aishuai.stats.service.StatsService;

/**
 * 统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
public class StatsController {
    
    @Autowired
    private StatsService statsService;
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/user")
    public Result<ExerciseStatsVO> getUserStats(
            HttpServletRequest request,
            @RequestParam(value = "subjectId", required = false) Long subjectId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauth("请登录后查看统计数据");
        }
        ExerciseStatsVO stats = statsService.getUserStatsVO(userId, subjectId);
        return Result.success(stats);
    }
}
