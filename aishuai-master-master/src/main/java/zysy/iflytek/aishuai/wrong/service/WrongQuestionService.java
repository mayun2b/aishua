package zysy.iflytek.aishuai.wrong.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zysy.iflytek.aishuai.question.entity.Question;
import zysy.iflytek.aishuai.wrong.vo.WrongQuestionVO;

import java.util.List;

/**
 * 错题服务接口
 */
public interface WrongQuestionService {
    
    /**
     * 分页查询错题列表
     */
    Page<WrongQuestionVO> pageWrongQuestions(Long userId, Integer pageNum, Integer pageSize, Long subjectId);
    
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
    Long getWrongQuestionCount(Long userId, Long subjectId);
    
    /**
     * 随机获取错题
     */
    List<Question> getRandomWrongQuestions(Long userId, Integer count, Long subjectId);
    
    /**
     * 获取所有错题
     */
    List<WrongQuestionVO> getAllWrongQuestions(Long userId, Long subjectId);
}
