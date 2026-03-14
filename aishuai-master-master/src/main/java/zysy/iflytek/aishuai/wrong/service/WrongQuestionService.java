package zysy.iflytek.aishuai.wrong.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zysy.iflytek.aishuai.question.entity.Question;

import java.util.List;

/**
 * 错题服务接口
 */
public interface WrongQuestionService {
    
    /**
     * 分页查询错题列表
     */
    Page<Question> pageWrongQuestions(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 添加错题
     */
    void addWrongQuestion(Long userId, Long questionId);
    
    /**
     * 移除错题
     */
    void removeWrongQuestion(Long userId, Long questionId);
    
    /**
     * 标记为已掌握
     */
    void markAsMastered(Long userId, Long questionId);
    
    /**
     * 获取错题数量
     */
    Long getWrongQuestionCount(Long userId);
    
    /**
     * 随机获取错题
     */
    List<Question> getRandomWrongQuestions(Long userId, Integer count);
}
