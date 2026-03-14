package zysy.iflytek.aishuai.exercise.service;

import zysy.iflytek.aishuai.exercise.dto.ExerciseRequest;
import zysy.iflytek.aishuai.exercise.vo.ExerciseResultVO;
import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;

import java.util.List;

/**
 * 练习服务接口
 */
public interface ExerciseService {
    
    /**
     * 提交答案
     */
    ExerciseResultVO submitAnswer(Long userId, ExerciseRequest request);
    
    /**
     * 批量提交答案
     */
    List<ExerciseResultVO> batchSubmitAnswers(Long userId, List<ExerciseRequest> requests);
    
    /**
     * 获取用户练习统计
     */
    ExerciseStatsVO getUserStats(Long userId);
    
    /**
     * 开始练习（获取题目列表）
     */
    List<zysy.iflytek.aishuai.question.entity.Question> startExercise(
            Integer count, 
            Long categoryId, 
            Integer difficulty, 
            Integer exerciseMode);

    /**
     * 仅检查答案（游客模式）
     */
    ExerciseResultVO checkAnswerOnly(ExerciseRequest request);
    
    /**
     * 批量仅检查答案（游客模式）
     */
    List<ExerciseResultVO> checkAnswersOnly(List<ExerciseRequest> requests);
}
