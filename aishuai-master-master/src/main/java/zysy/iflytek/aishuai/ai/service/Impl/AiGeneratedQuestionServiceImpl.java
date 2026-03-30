package zysy.iflytek.aishuai.ai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import zysy.iflytek.aishuai.ai.entity.AiGeneratedQuestion;
import zysy.iflytek.aishuai.ai.mapper.AiGeneratedQuestionMapper;
import zysy.iflytek.aishuai.ai.service.AiGeneratedQuestionService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI生成题目Service实现类
 */
@Service
public class AiGeneratedQuestionServiceImpl extends ServiceImpl<AiGeneratedQuestionMapper, AiGeneratedQuestion> implements AiGeneratedQuestionService {

    @Override
    public List<AiGeneratedQuestion> getAiGeneratedQuestionsByUserId(Long userId) {
        LambdaQueryWrapper<AiGeneratedQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiGeneratedQuestion::getUserId, userId)
                .orderByDesc(AiGeneratedQuestion::getGenerateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<AiGeneratedQuestion> getAiGeneratedQuestionsByUserIdAndSubjectId(Long userId, Long subjectId) {
        LambdaQueryWrapper<AiGeneratedQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiGeneratedQuestion::getUserId, userId)
                .eq(AiGeneratedQuestion::getSubjectId, subjectId)
                .orderByDesc(AiGeneratedQuestion::getGenerateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean generateAndSaveAiQuestion(AiGeneratedQuestion aiGeneratedQuestion) {
        // 设置生成时间
        aiGeneratedQuestion.setGenerateTime(LocalDateTime.now());
        // 设置未练习状态
        aiGeneratedQuestion.setIsPracticed(0);
        // 保存到数据库
        return save(aiGeneratedQuestion);
    }

    @Override
    public boolean updatePracticeStatus(Long id, Integer isPracticed) {
        AiGeneratedQuestion aiGeneratedQuestion = new AiGeneratedQuestion();
        aiGeneratedQuestion.setId(id);
        aiGeneratedQuestion.setIsPracticed(isPracticed);
        return updateById(aiGeneratedQuestion);
    }
}