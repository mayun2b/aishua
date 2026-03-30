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
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Long subjectId) {
        java.util.List<zysy.iflytek.aishuai.question.entity.Question> questions = 
                questionService.getRandomQuestions(count, categoryId, difficulty, subjectId);
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
    
    /**
     * 获取所有学科
     */
    @GetMapping("/subjects")
    public Result<List<zysy.iflytek.aishuai.question.entity.Subject>> getAllSubjects() {
        List<zysy.iflytek.aishuai.question.entity.Subject> subjects = 
                questionService.getAllSubjects();
        return Result.success(subjects);
    }
    
    /**
     * 根据学科 ID 获取题目分类
     */
    @GetMapping("/categories/{subjectId}")
    public Result<List<zysy.iflytek.aishuai.question.entity.QuestionCategory>> getCategoriesBySubjectId(@PathVariable Long subjectId) {
        List<zysy.iflytek.aishuai.question.entity.QuestionCategory> categories = 
                questionService.getCategoriesBySubjectId(subjectId);
        return Result.success(categories);
    }
    
    /**
     * 获取知识点进度
     */
    @GetMapping("/knowledge-points/progress")
    public Result<List<zysy.iflytek.aishuai.question.vo.KnowledgePointProgress>> getKnowledgePointProgress(
            @RequestParam Long subjectId,
            @RequestParam(required = false) Long userId) {
        List<zysy.iflytek.aishuai.question.vo.KnowledgePointProgress> progressList = 
                questionService.getKnowledgePointProgress(userId, subjectId);
        return Result.success(progressList);
    }
    
    /**
     * 批量修改题目学科
     */
    @PostMapping("/batch/update-subject")
    public Result<Void> batchUpdateSubject(@RequestBody BatchUpdateRequest request) {
        questionService.batchUpdateSubject(request.getQuestionIds(), request.getSubjectId());
        return Result.success(null);
    }
    
    /**
     * 批量修改题目分类
     */
    @PostMapping("/batch/update-category")
    public Result<Void> batchUpdateCategory(@RequestBody BatchUpdateRequest request) {
        questionService.batchUpdateCategory(request.getQuestionIds(), request.getCategoryId());
        return Result.success(null);
    }
    
    /**
     * 批量删除题目
     */
    @PostMapping("/batch/delete")
    public Result<Void> batchDeleteQuestions(@RequestBody BatchDeleteRequest request) {
        questionService.batchDeleteQuestions(request.getQuestionIds());
        return Result.success(null);
    }
    
    // 批量操作请求体
    static class BatchUpdateRequest {
        private List<Long> questionIds;
        private Long subjectId;
        private Long categoryId;
        
        // getters and setters
        public List<Long> getQuestionIds() {
            return questionIds;
        }
        public void setQuestionIds(List<Long> questionIds) {
            this.questionIds = questionIds;
        }
        public Long getSubjectId() {
            return subjectId;
        }
        public void setSubjectId(Long subjectId) {
            this.subjectId = subjectId;
        }
        public Long getCategoryId() {
            return categoryId;
        }
        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
    }
    
    static class BatchDeleteRequest {
        private List<Long> questionIds;
        
        // getters and setters
        public List<Long> getQuestionIds() {
            return questionIds;
        }
        public void setQuestionIds(List<Long> questionIds) {
            this.questionIds = questionIds;
        }
    }
}