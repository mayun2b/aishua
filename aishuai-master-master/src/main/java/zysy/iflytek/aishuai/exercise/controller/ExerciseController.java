package zysy.iflytek.aishuai.exercise.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.exercise.dto.ExerciseRequest;
import zysy.iflytek.aishuai.exercise.service.ExerciseService;
import zysy.iflytek.aishuai.exercise.vo.ExerciseResultVO;
import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;
import zysy.iflytek.aishuai.question.entity.Question;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 练习控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/exercise")
@Validated
public class ExerciseController {
    
    @Autowired
    private ExerciseService exerciseService;
    
    /**
     * 开始练习（获取题目列表）
     */
    @GetMapping("/start")
    public Result<List<Question>> startExercise(
            @RequestParam Integer count,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "1") Integer exerciseMode) {
        List<Question> questions = exerciseService.startExercise(count, categoryId, difficulty, exerciseMode);
        return Result.success(questions);
    }
    
    /**
     * 提交答案
     */
    @PostMapping("/submit")
    public Result<ExerciseResultVO> submitAnswer(@RequestBody @Validated ExerciseRequest request, HttpServletRequest httpServletRequest) {
        // 从登录信息中获取 userId
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        
        // 如果没有登录（游客模式），则只返回答案判断结果，不保存记录
        if (userId == null) {
            ExerciseResultVO result = exerciseService.checkAnswerOnly(request);
            return Result.success(result);
        }
        
        ExerciseResultVO result = exerciseService.submitAnswer(userId, request);
        return Result.success(result);
    }
    
    /**
     * 批量提交答案
     */
    @PostMapping("/batch-submit")
    public Result<List<ExerciseResultVO>> batchSubmitAnswers(@RequestBody List<ExerciseRequest> requests, HttpServletRequest httpServletRequest) {
        // 从登录信息中获取 userId
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        
        // 如果没有登录（游客模式），则只返回答案判断结果，不保存记录
        if (userId == null) {
            List<ExerciseResultVO> results = exerciseService.checkAnswersOnly(requests);
            return Result.success(results);
        }
        
        List<ExerciseResultVO> results = exerciseService.batchSubmitAnswers(userId, requests);
        return Result.success(results);
    }
    
    /**
     * 获取用户统计
     */
    @GetMapping("/stats")
    public Result<ExerciseStatsVO> getUserStats(HttpServletRequest httpServletRequest) {
        // 从登录信息中获取 userId
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        if (userId == null) {
            return Result.unauth("请登录后查看统计数据");
        }
        ExerciseStatsVO stats = exerciseService.getUserStats(userId);
        return Result.success(stats);
    }
}