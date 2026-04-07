package zysy.iflytek.aishuai.stats.service;

import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;

/**
 * 统计服务接口
 */
public interface StatsService {
    
    /**
     * 更新用户统计信息
     */
    void updateUserStats(Long userId, Boolean isCorrect);
    
    /**
     * 获取用户统计 VO
     */
    ExerciseStatsVO getUserStatsVO(Long userId, Long subjectId);
}
