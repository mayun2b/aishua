package zysy.iflytek.aishuai.wrong.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.question.entity.Question;
import zysy.iflytek.aishuai.wrong.service.WrongQuestionService;
import zysy.iflytek.aishuai.wrong.vo.WrongQuestionVO;

import java.util.List;

/**
 * 错题控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/wrong")
public class WrongQuestionController {
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    /**
     * 分页查询错题列表
     */
    @GetMapping("/page")
    public Result<Page<WrongQuestionVO>> pageWrongQuestions(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauth("用户未登录");
        }
        Page<WrongQuestionVO> page = wrongQuestionService.pageWrongQuestions(userId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 移除错题
     */
    @DeleteMapping("/{questionId}")
    public Result<Void> removeWrongQuestion(HttpServletRequest request, @PathVariable Long questionId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauth("用户未登录");
        }
        wrongQuestionService.removeWrongQuestion(userId, questionId);
        return Result.success(null);
    }
    
    /**
     * 标记为已掌握
     */
    @PutMapping("/{questionId}/mastered")
    public Result<Void> markAsMastered(HttpServletRequest request, @PathVariable Long questionId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauth("用户未登录");
        }
        wrongQuestionService.markAsMastered(userId, questionId);
        return Result.success(null);
    }
    
    /**
     * 获取错题数量
     */
    @GetMapping("/count")
    public Result<Long> getWrongQuestionCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauth("用户未登录");
        }
        Long count = wrongQuestionService.getWrongQuestionCount(userId);
        return Result.success(count);
    }
    
    /**
     * 随机获取错题（用于错题重练）
     */
    @GetMapping("/random")
    public Result<List<Question>> getRandomWrongQuestions(
            HttpServletRequest request,
            @RequestParam Integer count) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauth("用户未登录");
        }
        List<Question> questions = wrongQuestionService.getRandomWrongQuestions(userId, count);
        return Result.success(questions);
    }
}