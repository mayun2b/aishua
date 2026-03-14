package zysy.iflytek.aishuai.exercise.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishuai.exercise.dto.ExerciseRequest;
import zysy.iflytek.aishuai.exercise.entity.ExerciseRecord;
import zysy.iflytek.aishuai.exercise.mapper.ExerciseRecordMapper;
import zysy.iflytek.aishuai.exercise.service.ExerciseService;
import zysy.iflytek.aishuai.exercise.vo.ExerciseResultVO;
import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;
import zysy.iflytek.aishuai.question.entity.Question;
import zysy.iflytek.aishuai.question.service.QuestionService;
import zysy.iflytek.aishuai.question.vo.QuestionVO;
import zysy.iflytek.aishuai.stats.service.StatsService;
import zysy.iflytek.aishuai.wrong.service.WrongQuestionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 练习服务实现
 */
@Slf4j
@Service
public class ExerciseServiceImpl implements ExerciseService {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExerciseRecordMapper exerciseRecordMapper;

    @Autowired
    private StatsService statsService;

    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExerciseResultVO submitAnswer(Long userId, ExerciseRequest request) {
        log.info("开始提交答案 - userId: {}, questionId: {}, userAnswer: {}", userId, request.getQuestionId(), request.getUserAnswer());
        
        try {
            // 1. 查询题目详情
            zysy.iflytek.aishuai.question.vo.QuestionVO questionVO;
            try {
                questionVO = questionService.getQuestionById(request.getQuestionId());
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("题目不存在")) {
                    log.warn("题目不存在 - questionId: {}", request.getQuestionId());
                    ExerciseResultVO errorResult = new ExerciseResultVO();
                    errorResult.setIsCorrect(false);
                    errorResult.setCorrectAnswer("题目不存在");
                    errorResult.setAnalysis("该题目已被删除或不存在");
                    return errorResult;
                }
                throw e;
            }
            
            log.info("获取题目成功 - questionId: {}", request.getQuestionId());
            
            // 2. 判断答案是否正确
            boolean isCorrect = checkAnswer(questionVO, request.getUserAnswer());
            log.info("答案判断完成 - isCorrect: {}", isCorrect);
            
            // 3. 保存练习记录
            ExerciseRecord record = new ExerciseRecord();
            record.setUserId(userId);
            record.setQuestionId(request.getQuestionId());
            record.setUserAnswer(request.getUserAnswer());
            record.setIsCorrect(isCorrect ? 1 : 0);
            record.setTimeCost(request.getTimeCost() != null ? request.getTimeCost() : 0);
            record.setExerciseMode(request.getExerciseMode());
            record.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
            record.setExerciseTime(LocalDateTime.now());
            
            exerciseRecordMapper.insert(record);
            log.info("保存练习记录成功 - recordId: {}", record.getId());
            
            // 4. 更新题目统计
            questionService.updateQuestionStats(request.getQuestionId(), isCorrect);
            log.info("更新题目统计成功");
            
            // 5. 更新用户统计
            statsService.updateUserStats(userId, isCorrect);
            log.info("更新用户统计成功");
            
            // 6. 如果错误，加入错题本
            if (!isCorrect) {
                wrongQuestionService.addWrongQuestion(userId, request.getQuestionId());
                log.info("添加错题成功");
            }
            
            // 7. 构建返回结果
            ExerciseResultVO result = new ExerciseResultVO();
            result.setRecordId(record.getId());
            result.setIsCorrect(isCorrect);
            result.setCorrectAnswer(questionVO.getAnswer());
            result.setAnalysis(questionVO.getAnalysis());
            result.setTimeCost(record.getTimeCost());
            
            log.info("提交答案完成 - userId: {}, questionId: {}", userId, request.getQuestionId());
            return result;
        } catch (Exception e) {
            log.error("提交答案失败 - userId: {}, request: {}", userId, request, e);
            throw new RuntimeException("提交答案失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ExerciseResultVO> batchSubmitAnswers(Long userId, List<ExerciseRequest> requests) {
        log.info("开始批量提交答案 - userId: {}, requestCount: {}", userId, requests.size());
        List<ExerciseResultVO> results = new ArrayList<>();
        for (ExerciseRequest request : requests) {
            try {
                ExerciseResultVO result = submitAnswer(userId, request);
                results.add(result);
            } catch (Exception e) {
                log.error("批量提交单个答案失败 - userId: {}, request: {}", userId, request, e);
                // 创建错误结果
                ExerciseResultVO errorResult = new ExerciseResultVO();
                errorResult.setIsCorrect(false);
                errorResult.setCorrectAnswer("系统错误");
                errorResult.setAnalysis("提交过程中发生错误: " + e.getMessage());
                results.add(errorResult);
            }
        }
        log.info("批量提交完成 - userId: {}, successCount: {}", userId, results.size());
        return results;
    }
    
    @Override
    public ExerciseStatsVO getUserStats(Long userId) {
        return statsService.getUserStatsVO(userId);
    }
    
    @Override
    public List<zysy.iflytek.aishuai.question.entity.Question> startExercise(Integer count, Long categoryId, Integer difficulty, Integer exerciseMode) {
        // 根据不同模式获取题目
        if (exerciseMode == 3) {
            // 错题模式 - 需要从错题本获取题目（游客无法使用此功能）
            return new ArrayList<>();
        }

        // 其他模式：随机获取题目
        return questionService.getRandomQuestions(count, categoryId, difficulty);
    }

    @Override
    public ExerciseResultVO checkAnswerOnly(ExerciseRequest request) {
        log.info("开始检查答案（游客模式）- questionId: {}, userAnswer: {}", request.getQuestionId(), request.getUserAnswer());

        try {
            // 1. 查询题目详情
            QuestionVO questionVO = questionService.getQuestionById(request.getQuestionId());

            log.info("获取题目成功 - questionId: {}", request.getQuestionId());

            // 2. 判断答案是否正确
            boolean isCorrect = checkAnswer(questionVO, request.getUserAnswer());
            log.info("答案判断完成 - isCorrect: {}", isCorrect);

            // 3. 构建返回结果（不保存记录）
            ExerciseResultVO result = new ExerciseResultVO();
            result.setIsCorrect(isCorrect);
            result.setCorrectAnswer(questionVO.getAnswer());
            result.setAnalysis(questionVO.getAnalysis());
            result.setTimeCost(request.getTimeCost() != null ? request.getTimeCost() : 0);

            log.info("检查答案完成（游客模式）- questionId: {}", request.getQuestionId());
            return result;
        } catch (Exception e) {
            log.error("检查答案失败（游客模式）- request: {}", request, e);
            throw new RuntimeException("检查答案失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExerciseResultVO> checkAnswersOnly(List<ExerciseRequest> requests) {
        List<ExerciseResultVO> results = new ArrayList<>();
        for (ExerciseRequest request : requests) {
            try {
                ExerciseResultVO result = checkAnswerOnly(request);
                results.add(result);
            } catch (Exception e) {
                log.error("批量检查答案失败（游客模式）：{}", e.getMessage());
            }
        }
        return results;
    }

    /**
     * 判断答案是否正确
     */
    private boolean checkAnswer(QuestionVO questionVO, String userAnswer) {
        if (userAnswer == null || questionVO.getAnswer() == null) {
            return false;
        }

        String correctAnswer = questionVO.getAnswer();
        String userAns = userAnswer.trim();

        // 如果是多选题，需要比较排序后的字符
        if (questionVO.getType() == 2) {
            char[] correctChars = correctAnswer.toCharArray();
            char[] userChars = userAns.toUpperCase().toCharArray();
            java.util.Arrays.sort(correctChars);
            java.util.Arrays.sort(userChars);
            return new String(correctChars).equals(new String(userChars));
        }

        // 根据题型判断
        switch (questionVO.getType()) {
            case 1: // 单选题
            case 3: // 判断题
                return correctAnswer.trim().equalsIgnoreCase(userAns);
            case 4: // 填空题
                return checkFillBlank(correctAnswer, userAns);
            case 5: // 简答题
                // 简答题需要人工评分，这里暂时认为都正确
                return true;
            default:
                return false;
        }
    }

    /**
     * 检查填空题答案
     */
    private boolean checkFillBlank(String correctAnswer, String userAnswer) {
        // 简单实现：完全匹配
        // TODO: 可以优化为支持多个空的答案判断
        return correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
    }
}
