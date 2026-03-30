package zysy.iflytek.aishuai.question.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zysy.iflytek.aishuai.question.dto.QuestionCreateDTO;
import zysy.iflytek.aishuai.question.dto.QuestionQueryDTO;
import zysy.iflytek.aishuai.question.entity.Question;
import zysy.iflytek.aishuai.question.entity.QuestionCategory;
import zysy.iflytek.aishuai.question.entity.Subject;
import zysy.iflytek.aishuai.question.vo.KnowledgePointProgress;
import zysy.iflytek.aishuai.question.vo.QuestionVO;

import java.util.List;

/**
 * 题目服务接口
 */
public interface QuestionService {
    
    /**
     * 分页查询题目
     */
    Page<QuestionVO> pageQuestions(QuestionQueryDTO queryDTO);
    
    /**
     * 根据 ID 查询题目详情
     */
    QuestionVO getQuestionById(Long id);
    
    /**
     * 创建题目
     */
    Long createQuestion(QuestionCreateDTO dto);
    
    /**
     * 更新题目
     */
    void updateQuestion(Long id, QuestionCreateDTO dto);
    
    /**
     * 删除题目
     */
    void deleteQuestion(Long id);
    
    /**
     * 获取随机题目
     */
    List<Question> getRandomQuestions(Integer count, Long categoryId, Integer difficulty, Long subjectId);
    
    /**
     * 更新题目统计信息
     */
    void updateQuestionStats(Long questionId, Boolean isCorrect);
    
    /**
     * 获取所有题目分类
     */
    List<QuestionCategory> getAllCategories();
    
    /**
     * 根据学科 ID 获取题目分类
     */
    List<QuestionCategory> getCategoriesBySubjectId(Long subjectId);
    
    /**
     * 获取所有学科
     */
    List<Subject> getAllSubjects();
    
    /**
     * 获取知识点进度
     */
    List<KnowledgePointProgress> getKnowledgePointProgress(Long userId, Long subjectId);
    
    /**
     * 批量修改题目学科
     */
    void batchUpdateSubject(List<Long> questionIds, Long subjectId);
    
    /**
     * 批量修改题目分类
     */
    void batchUpdateCategory(List<Long> questionIds, Long categoryId);
    
    /**
     * 批量删除题目
     */
    void batchDeleteQuestions(List<Long> questionIds);
}