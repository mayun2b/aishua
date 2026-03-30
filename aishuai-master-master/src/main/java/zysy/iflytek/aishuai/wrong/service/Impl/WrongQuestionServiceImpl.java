package zysy.iflytek.aishuai.wrong.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishuai.question.entity.Question;
import zysy.iflytek.aishuai.question.entity.QuestionCategory;
import zysy.iflytek.aishuai.question.mapper.QuestionCategoryMapper;
import zysy.iflytek.aishuai.question.mapper.QuestionMapper;
import zysy.iflytek.aishuai.wrong.entity.WrongQuestion;
import zysy.iflytek.aishuai.wrong.mapper.WrongQuestionMapper;
import zysy.iflytek.aishuai.wrong.service.WrongQuestionService;
import zysy.iflytek.aishuai.wrong.vo.WrongQuestionVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题服务实现
 */
@Slf4j
@Service
public class WrongQuestionServiceImpl implements WrongQuestionService {
    
    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private QuestionCategoryMapper questionCategoryMapper;
    
    @Override
    public Page<WrongQuestionVO> pageWrongQuestions(Long userId, Integer pageNum, Integer pageSize, Long subjectId) {
        // 1. 查询用户的错题
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
               .eq(WrongQuestion::getMasterStatus, 0); // 只查询未掌握的
        
        Page<WrongQuestion> wrongPage = wrongQuestionMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        
        // 2. 构建错题 VO 列表
        List<WrongQuestionVO> voList = new ArrayList<>();
        for (WrongQuestion wrongQuestion : wrongPage.getRecords()) {
            Question question = questionMapper.selectById(wrongQuestion.getQuestionId());
            if (question != null) {
                // 如果指定了学科，且题目的学科不匹配，则跳过
                if (subjectId != null && !subjectId.equals(question.getSubjectId())) {
                    continue;
                }
                
                // 获取分类名称
                String categoryName = "";
                if (question.getCategoryId() != null) {
                    QuestionCategory category = questionCategoryMapper.selectById(question.getCategoryId());
                    if (category != null) {
                        categoryName = category.getName();
                    }
                }
                
                WrongQuestionVO vo = WrongQuestionVO.fromQuestionAndWrongQuestion(
                        question, 
                        wrongQuestion.getWrongCount(), 
                        wrongQuestion.getLastWrongTime(), 
                        wrongQuestion.getMasterStatus(),
                        categoryName
                );
                voList.add(vo);
            }
        }
        
        // 3. 构建分页结果
        Page<WrongQuestionVO> resultPage = new Page<>();
        resultPage.setCurrent(wrongPage.getCurrent());
        resultPage.setSize(wrongPage.getSize());
        resultPage.setTotal(wrongPage.getTotal());
        resultPage.setRecords(voList);
        
        return resultPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWrongQuestion(Long userId, Long questionId) {
        // 检查是否已存在
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
               .eq(WrongQuestion::getQuestionId, questionId);
        
        WrongQuestion existing = wrongQuestionMapper.selectOne(wrapper);
        
        if (existing != null) {
            // 已存在，增加错误次数
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setLastWrongTime(LocalDateTime.now());
            wrongQuestionMapper.updateById(existing);
        } else {
            // 不存在，新增
            WrongQuestion wrongQuestion = new WrongQuestion();
            wrongQuestion.setUserId(userId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setLastWrongTime(LocalDateTime.now());
            wrongQuestion.setMasterStatus(0);
            wrongQuestionMapper.insert(wrongQuestion);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWrongQuestion(Long userId, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
               .eq(WrongQuestion::getQuestionId, questionId);
        
        wrongQuestionMapper.delete(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsMastered(Long userId, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
               .eq(WrongQuestion::getQuestionId, questionId);
        
        WrongQuestion wrongQuestion = wrongQuestionMapper.selectOne(wrapper);
        if (wrongQuestion != null) {
            wrongQuestion.setMasterStatus(1);
            wrongQuestionMapper.updateById(wrongQuestion);
        }
    }
    
    @Override
    public Long getWrongQuestionCount(Long userId, Long subjectId) {
        // 获取所有错题ID
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
               .eq(WrongQuestion::getMasterStatus, 0);
        
        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);
        
        // 统计符合条件的错题数量
        return wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .map(questionMapper::selectById)
                .filter(question -> {
                    if (question == null) {
                        return false;
                    }
                    // 如果指定了学科，且题目的学科不匹配，则过滤掉
                    if (subjectId != null && !subjectId.equals(question.getSubjectId())) {
                        return false;
                    }
                    return true;
                })
                .count();
    }
    
    @Override
    public List<Question> getRandomWrongQuestions(Long userId, Integer count, Long subjectId) {
        // 获取用户的所有错题
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
               .eq(WrongQuestion::getMasterStatus, 0);
        
        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);
        
        // 随机打乱并取指定数量，同时确保题目存在且符合学科要求
        return wrongQuestions.stream()
                .map(wq -> questionMapper.selectById(wq.getQuestionId()))
                .filter(question -> {
                    if (question == null) {
                        return false;
                    }
                    // 如果指定了学科，且题目的学科不匹配，则过滤掉
                    if (subjectId != null && !subjectId.equals(question.getSubjectId())) {
                        return false;
                    }
                    return true;
                })
                .sorted((a, b) -> Math.random() > 0.5 ? 1 : -1)
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public List<WrongQuestionVO> getAllWrongQuestions(Long userId, Long subjectId) {
        // 获取用户的所有错题（包括已掌握和未掌握的）
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId);
        
        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(wrapper);
        
        // 构建错题VO列表
        List<WrongQuestionVO> voList = new ArrayList<>();
        for (WrongQuestion wrongQuestion : wrongQuestions) {
            Question question = questionMapper.selectById(wrongQuestion.getQuestionId());
            if (question != null) {
                // 如果指定了学科，且题目的学科不匹配，则跳过
                if (subjectId != null && !subjectId.equals(question.getSubjectId())) {
                    continue;
                }
                
                // 获取分类名称
                String categoryName = "";
                if (question.getCategoryId() != null) {
                    QuestionCategory category = questionCategoryMapper.selectById(question.getCategoryId());
                    if (category != null) {
                        categoryName = category.getName();
                    }
                }
                
                WrongQuestionVO vo = WrongQuestionVO.fromQuestionAndWrongQuestion(
                        question, 
                        wrongQuestion.getWrongCount(), 
                        wrongQuestion.getLastWrongTime(), 
                        wrongQuestion.getMasterStatus(),
                        categoryName
                );
                voList.add(vo);
            }
        }
        
        return voList;
    }
}
