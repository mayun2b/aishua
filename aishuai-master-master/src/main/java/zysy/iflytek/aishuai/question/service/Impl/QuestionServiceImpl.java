package zysy.iflytek.aishuai.question.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishuai.question.dto.QuestionCreateDTO;
import zysy.iflytek.aishuai.question.dto.QuestionQueryDTO;
import zysy.iflytek.aishuai.question.entity.Question;
import zysy.iflytek.aishuai.question.entity.QuestionCategory;
import zysy.iflytek.aishuai.question.entity.Subject;
import zysy.iflytek.aishuai.question.mapper.QuestionMapper;
import zysy.iflytek.aishuai.question.mapper.QuestionCategoryMapper;
import zysy.iflytek.aishuai.question.mapper.SubjectMapper;
import zysy.iflytek.aishuai.question.service.QuestionService;
import zysy.iflytek.aishuai.question.vo.QuestionVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 */
@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionCategoryMapper questionCategoryMapper;
    
    @Autowired
    private SubjectMapper subjectMapper;
    
    @Override
    public Page<QuestionVO> pageQuestions(QuestionQueryDTO queryDTO) {
        Page<Question> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Question::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getSubjectId() != null) {
            wrapper.eq(Question::getSubjectId, queryDTO.getSubjectId());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(Question::getType, queryDTO.getType());
        }
        if (queryDTO.getDifficulty() != null) {
            wrapper.eq(Question::getDifficulty, queryDTO.getDifficulty());
        }
        
        Page<Question> resultPage = questionMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        Page<QuestionVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage, "records");
        voPage.setRecords(resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }
    
    @Override
    public QuestionVO getQuestionById(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        return convertToVO(question);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(QuestionCreateDTO dto) {
        Question question = new Question();
        BeanUtils.copyProperties(dto, question);
        question.setDoCount(0);
        question.setCorrectRate(0.0);
        
        questionMapper.insert(question);
        return question.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long id, QuestionCreateDTO dto) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        
        BeanUtils.copyProperties(dto, question, "id", "doCount", "correctRate");
        questionMapper.updateById(question);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
    }
    
    @Override
    public List<Question> getRandomQuestions(Integer count, Long categoryId, Integer difficulty) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            // 检查是否为父分类，如果是，获取所有子分类的题目
            List<QuestionCategory> categories = questionCategoryMapper.selectList(
                    new LambdaQueryWrapper<QuestionCategory>().eq(QuestionCategory::getParentId, categoryId)
            );
            if (!categories.isEmpty()) {
                // 如果是父分类，获取所有子分类的ID
                List<Long> categoryIds = categories.stream()
                        .map(QuestionCategory::getId)
                        .collect(Collectors.toList());
                // 同时包含父分类和子分类的题目
                categoryIds.add(categoryId);
                wrapper.in(Question::getCategoryId, categoryIds);
            } else {
                // 如果是子分类，直接匹配
                wrapper.eq(Question::getCategoryId, categoryId);
            }
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        wrapper.orderByDesc(Question::getId);
        
        List<Question> questions = questionMapper.selectList(wrapper);
        // 随机打乱并取指定数量
        return questions.stream()
                .sorted((a, b) -> Math.random() > 0.5 ? 1 : -1)
                .limit(count)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionStats(Long questionId, Boolean isCorrect) {
        Question question = questionMapper.selectById(questionId);
        if (question != null) {
            int oldDoCount = question.getDoCount();
            int newDoCount = oldDoCount + 1;
            
            // 计算新的正确次数
            double oldCorrectRate = question.getCorrectRate();
            int oldCorrectCount = (int) Math.round(oldCorrectRate * oldDoCount / 100.0);
            int newCorrectCount = oldCorrectCount + (isCorrect ? 1 : 0);
            
            // 计算新的正确率（百分比）
            double newCorrectRate = (newDoCount > 0) ? (double) newCorrectCount / newDoCount * 100 : 0.0;
            
            // 确保正确率在合理范围内 [0, 100]
            if (newCorrectRate < 0) newCorrectRate = 0;
            if (newCorrectRate > 100) newCorrectRate = 100;
            
            question.setDoCount(newDoCount);
            question.setCorrectRate(newCorrectRate);
            
            questionMapper.updateById(question);
        }
    }
    
    /**
     * 转换为 VO
     */
    private QuestionVO convertToVO(Question question) {
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        
        // 设置分类名称
        if (question.getCategoryId() != null) {
            QuestionCategory category = questionCategoryMapper.selectById(question.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        // 设置学科名称
        if (question.getSubjectId() != null) {
            Subject subject = subjectMapper.selectById(question.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getName());
            }
        }
        
        return vo;
    }
    
    @Override
    public List<QuestionCategory> getAllCategories() {
        LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(QuestionCategory::getSort);
        return questionCategoryMapper.selectList(wrapper);
    }
    
    @Override
    public List<Subject> getAllSubjects() {
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Subject::getSort);
        return subjectMapper.selectList(wrapper);
    }
}