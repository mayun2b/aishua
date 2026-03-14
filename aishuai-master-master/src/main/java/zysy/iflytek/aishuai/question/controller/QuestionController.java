package zysy.iflytek.aishuai.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.question.dto.QuestionCreateDTO;
import zysy.iflytek.aishuai.question.dto.QuestionQueryDTO;
import zysy.iflytek.aishuai.question.service.QuestionService;
import zysy.iflytek.aishuai.question.vo.QuestionVO;

import java.util.List;

/**
 * 题目控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/question")
@Validated
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    /**
     * 分页查询题目列表
     */
    @GetMapping("/page")
    public Result<Page<QuestionVO>> pageQuestions(QuestionQueryDTO queryDTO) {
        Page<QuestionVO> page = questionService.pageQuestions(queryDTO);
        return Result.success(page);
    }
    
    /**
     * 根据 ID 查询题目详情
     */
    @GetMapping("/{id}")
    public Result<QuestionVO> getQuestionById(@PathVariable Long id) {
        QuestionVO vo = questionService.getQuestionById(id);
        return Result.success(vo);
    }
    
    /**
     * 创建题目
     */
    @PostMapping
    public Result<Long> createQuestion(@RequestBody @Validated QuestionCreateDTO dto) {
        Long id = questionService.createQuestion(dto);
        return Result.success(id);
    }
    
    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public Result<Void> updateQuestion(@PathVariable Long id, 
                                       @RequestBody @Validated QuestionCreateDTO dto) {
        questionService.updateQuestion(id, dto);
        return Result.success(null);
    }
    
    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success(null);
    }
    
    /**
     * 随机获取题目
     */
    @GetMapping("/random")
    public Result<java.util.List<zysy.iflytek.aishuai.question.entity.Question>> getRandomQuestions(
            @RequestParam Integer count,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer difficulty) {
        java.util.List<zysy.iflytek.aishuai.question.entity.Question> questions = 
                questionService.getRandomQuestions(count, categoryId, difficulty);
        return Result.success(questions);
    }
    
    /**
     * 获取所有题目分类
     */
    @GetMapping("/categories")
    public Result<List<zysy.iflytek.aishuai.question.entity.QuestionCategory>> getAllCategories() {
        List<zysy.iflytek.aishuai.question.entity.QuestionCategory> categories = 
                questionService.getAllCategories();
        return Result.success(categories);
    }
}