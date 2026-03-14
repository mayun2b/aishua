package zysy.iflytek.aishuai.stats.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishuai.exercise.vo.ExerciseStatsVO;
import zysy.iflytek.aishuai.stats.entity.UserStats;
import zysy.iflytek.aishuai.stats.mapper.UserStatsMapper;
import zysy.iflytek.aishuai.stats.service.StatsService;
import zysy.iflytek.aishuai.wrong.service.WrongQuestionService;

import java.time.LocalDate;

/**
 * 统计服务实现
 */
@Slf4j
@Service
public class StatsServiceImpl implements StatsService {
    
    @Autowired
    private UserStatsMapper userStatsMapper;
    
    @Autowired
    private WrongQuestionService wrongQuestionService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStats(Long userId, Boolean isCorrect) {
        // 1. 查询或创建用户统计
        UserStats stats = getUserStatsOrCreate(userId);
        
        // 2. 更新统计数据
        stats.setTotalCount(stats.getTotalCount() + 1);
        if (isCorrect) {
            stats.setCorrectCount(stats.getCorrectCount() + 1);
        } else {
            stats.setWrongCount(stats.getWrongCount() + 1);
        }
        
        // 3. 更新连续天数
        LocalDate today = LocalDate.now();
        if (stats.getLastExerciseDate() == null) {
            // 第一次练习
            stats.setContinuousDays(1);
            stats.setMaxContinuousDays(1);
        } else if (stats.getLastExerciseDate().equals(today.minusDays(1))) {
            // 昨天练习过，连续天数 +1
            stats.setContinuousDays(stats.getContinuousDays() + 1);
            if (stats.getContinuousDays() > stats.getMaxContinuousDays()) {
                stats.setMaxContinuousDays(stats.getContinuousDays());
            }
        } else if (!stats.getLastExerciseDate().equals(today)) {
            // 不是今天，且距离上次练习超过 1 天，重置连续天数
            stats.setContinuousDays(1);
        }
        
        // 4. 更新最后练习日期
        stats.setLastExerciseDate(today);
        
        userStatsMapper.updateById(stats);
    }
    
    @Override
    public ExerciseStatsVO getUserStatsVO(Long userId) {
        UserStats stats = getUserStatsOrCreate(userId);
        
        ExerciseStatsVO vo = new ExerciseStatsVO();
        vo.setTotalCount(stats.getTotalCount());
        vo.setCorrectCount(stats.getCorrectCount());
        vo.setWrongCount(stats.getWrongCount());
        
        // 计算正确率
        if (stats.getTotalCount() > 0) {
            vo.setCorrectRate(stats.getCorrectCount() * 100.0 / stats.getTotalCount());
        } else {
            vo.setCorrectRate(0.0);
        }
        
        vo.setContinuousDays(stats.getContinuousDays());
        
        return vo;
    }
    
    /**
     * 获取用户统计，如果不存在则创建
     */
    private UserStats getUserStatsOrCreate(Long userId) {
        UserStats stats = userStatsMapper.selectOne(
                new LambdaQueryWrapper<UserStats>()
                        .eq(UserStats::getUserId, userId)
        );
        
        if (stats == null) {
            stats = new UserStats();
            stats.setUserId(userId);
            stats.setTotalCount(0);
            stats.setCorrectCount(0);
            stats.setWrongCount(0);
            stats.setContinuousDays(0);
            stats.setMaxContinuousDays(0);
            userStatsMapper.insert(stats);
        }
        
        return stats;
    }
}
