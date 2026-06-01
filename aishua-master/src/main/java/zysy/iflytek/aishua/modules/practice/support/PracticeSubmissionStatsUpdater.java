package zysy.iflytek.aishua.modules.practice.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Component;
import zysy.iflytek.aishua.config.properties.PracticeMasteryProperties;
import zysy.iflytek.aishua.modules.practice.entity.DailyStats;
import zysy.iflytek.aishua.modules.practice.entity.UserKnowledgeMastery;
import zysy.iflytek.aishua.modules.practice.entity.UserStats;
import zysy.iflytek.aishua.modules.practice.entity.UserSubjectStats;
import zysy.iflytek.aishua.modules.practice.mapper.DailyStatsMapper;
import zysy.iflytek.aishua.modules.practice.mapper.UserKnowledgeMasteryMapper;
import zysy.iflytek.aishua.modules.practice.mapper.UserStatsMapper;
import zysy.iflytek.aishua.modules.practice.mapper.UserSubjectStatsMapper;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * 练习提交后的统计更新组件。
 * 统一负责题目、用户、学科、日统计与知识点掌握度的落库更新。
 */
@Component
public class PracticeSubmissionStatsUpdater {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int DEFAULT_MASTERY_MIN_SAMPLE_COUNT = 20;
    private static final int DEFAULT_MASTERY_RATE_LEVEL_1 = 60;
    private static final int DEFAULT_MASTERY_RATE_LEVEL_2 = 75;
    private static final int DEFAULT_MASTERY_RATE_LEVEL_3 = 90;
    private static final int DEFAULT_MASTERY_WARMUP_LEVEL_CAP = 2;

    private final QuestionMapper questionMapper;
    private final UserStatsMapper userStatsMapper;
    private final UserSubjectStatsMapper userSubjectStatsMapper;
    private final DailyStatsMapper dailyStatsMapper;
    private final UserKnowledgeMasteryMapper userKnowledgeMasteryMapper;
    private final PracticeMasteryProperties practiceMasteryProperties;

    public PracticeSubmissionStatsUpdater(
            QuestionMapper questionMapper,
            UserStatsMapper userStatsMapper,
            UserSubjectStatsMapper userSubjectStatsMapper,
            DailyStatsMapper dailyStatsMapper,
            UserKnowledgeMasteryMapper userKnowledgeMasteryMapper,
            PracticeMasteryProperties practiceMasteryProperties
    ) {
        this.questionMapper = questionMapper;
        this.userStatsMapper = userStatsMapper;
        this.userSubjectStatsMapper = userSubjectStatsMapper;
        this.dailyStatsMapper = dailyStatsMapper;
        this.userKnowledgeMasteryMapper = userKnowledgeMasteryMapper;
        this.practiceMasteryProperties = practiceMasteryProperties;
    }

    /**
     * 单题判分后执行统计更新，保证聚合写入逻辑只在一处维护。
     */
    public void applyAnswerStats(
            Long userId,
            Long subjectId,
            Question question,
            List<Long> tagIds,
            boolean isCorrect,
            int timeCost
    ) {
        updateQuestionStats(question, isCorrect);
        updateUserStats(userId, isCorrect);
        updateUserSubjectStats(userId, subjectId, isCorrect, timeCost);
        updateDailyStats(userId, isCorrect, timeCost);
        updateKnowledgeMastery(userId, question.getSubjectId(), tagIds, isCorrect);
    }

    private void updateQuestionStats(Question question, boolean isCorrect) {
        int previousDoCount = defaultNumber(question.getDoCount());
        BigDecimal previousRate = defaultDecimal(question.getCorrectRate());
        BigDecimal previousCorrectEquivalent = previousRate
                .multiply(BigDecimal.valueOf(previousDoCount))
                .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);

        int newDoCount = previousDoCount + 1;
        BigDecimal newCorrectEquivalent = previousCorrectEquivalent.add(isCorrect ? BigDecimal.ONE : BigDecimal.ZERO);
        BigDecimal newRate = newCorrectEquivalent
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(newDoCount), 2, RoundingMode.HALF_UP);

        question.setDoCount(newDoCount);
        question.setCorrectRate(newRate);
        questionMapper.updateById(question);
    }

    private void updateUserStats(Long userId, boolean isCorrect) {
        UserStats userStats = userStatsMapper.selectOne(new LambdaQueryWrapper<UserStats>()
                .eq(UserStats::getUserId, userId)
                .last("LIMIT 1"));
        if (userStats == null) {
            userStats = new UserStats();
            userStats.setUserId(userId);
            userStats.setTotalCount(0);
            userStats.setCorrectCount(0);
            userStats.setWrongCount(0);
            userStats.setCorrectRate(BigDecimal.ZERO);
        }

        userStats.setTotalCount(defaultNumber(userStats.getTotalCount()) + 1);
        userStats.setCorrectCount(defaultNumber(userStats.getCorrectCount()) + (isCorrect ? 1 : 0));
        userStats.setWrongCount(defaultNumber(userStats.getWrongCount()) + (isCorrect ? 0 : 1));
        userStats.setCorrectRate(calculateRate(userStats.getCorrectCount(), userStats.getTotalCount()));
        userStats.setLastExerciseDate(LocalDate.now());

        if (userStats.getId() == null) {
            userStatsMapper.insert(userStats);
        } else {
            userStatsMapper.updateById(userStats);
        }
    }

    private void updateUserSubjectStats(Long userId, Long subjectId, boolean isCorrect, int timeCost) {
        UserSubjectStats subjectStats = userSubjectStatsMapper.selectOne(new LambdaQueryWrapper<UserSubjectStats>()
                .eq(UserSubjectStats::getUserId, userId)
                .eq(UserSubjectStats::getSubjectId, subjectId)
                .last("LIMIT 1"));
        if (subjectStats == null) {
            subjectStats = new UserSubjectStats();
            subjectStats.setUserId(userId);
            subjectStats.setSubjectId(subjectId);
            subjectStats.setTotalCount(0);
            subjectStats.setCorrectCount(0);
            subjectStats.setWrongCount(0);
            subjectStats.setCorrectRate(BigDecimal.ZERO);
            subjectStats.setTotalTimeCost(0);
        }

        subjectStats.setTotalCount(defaultNumber(subjectStats.getTotalCount()) + 1);
        subjectStats.setCorrectCount(defaultNumber(subjectStats.getCorrectCount()) + (isCorrect ? 1 : 0));
        subjectStats.setWrongCount(defaultNumber(subjectStats.getWrongCount()) + (isCorrect ? 0 : 1));
        subjectStats.setCorrectRate(calculateRate(subjectStats.getCorrectCount(), subjectStats.getTotalCount()));
        subjectStats.setTotalTimeCost(defaultNumber(subjectStats.getTotalTimeCost()) + timeCost);
        subjectStats.setLastPracticeDate(LocalDate.now());

        if (subjectStats.getId() == null) {
            userSubjectStatsMapper.insert(subjectStats);
        } else {
            userSubjectStatsMapper.updateById(subjectStats);
        }
    }

    private void updateDailyStats(Long userId, boolean isCorrect, int timeCost) {
        LocalDate today = LocalDate.now();
        DailyStats dailyStats = dailyStatsMapper.selectOne(new LambdaQueryWrapper<DailyStats>()
                .eq(DailyStats::getUserId, userId)
                .eq(DailyStats::getStatDate, today)
                .last("LIMIT 1"));
        if (dailyStats == null) {
            dailyStats = new DailyStats();
            dailyStats.setUserId(userId);
            dailyStats.setStatDate(today);
            dailyStats.setDoCount(0);
            dailyStats.setCorrectCount(0);
            dailyStats.setTimeCost(0);
        }

        dailyStats.setDoCount(defaultNumber(dailyStats.getDoCount()) + 1);
        dailyStats.setCorrectCount(defaultNumber(dailyStats.getCorrectCount()) + (isCorrect ? 1 : 0));
        dailyStats.setTimeCost(defaultNumber(dailyStats.getTimeCost()) + timeCost);

        if (dailyStats.getId() == null) {
            dailyStatsMapper.insert(dailyStats);
        } else {
            dailyStatsMapper.updateById(dailyStats);
        }
    }

    private void updateKnowledgeMastery(Long userId, Long subjectId, List<Long> tagIds, boolean isCorrect) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        for (Long tagId : tagIds) {
            UserKnowledgeMastery knowledgeMastery = userKnowledgeMasteryMapper.selectOne(new LambdaQueryWrapper<UserKnowledgeMastery>()
                    .eq(UserKnowledgeMastery::getUserId, userId)
                    .eq(UserKnowledgeMastery::getTagId, tagId)
                    .last("LIMIT 1"));
            if (knowledgeMastery == null) {
                knowledgeMastery = new UserKnowledgeMastery();
                knowledgeMastery.setUserId(userId);
                knowledgeMastery.setTagId(tagId);
                knowledgeMastery.setSubjectId(subjectId);
                knowledgeMastery.setTotalCount(0);
                knowledgeMastery.setCorrectCount(0);
                knowledgeMastery.setWrongCount(0);
                knowledgeMastery.setCorrectRate(BigDecimal.ZERO);
                knowledgeMastery.setMasteryLevel(0);
            }

            knowledgeMastery.setTotalCount(defaultNumber(knowledgeMastery.getTotalCount()) + 1);
            knowledgeMastery.setCorrectCount(defaultNumber(knowledgeMastery.getCorrectCount()) + (isCorrect ? 1 : 0));
            knowledgeMastery.setWrongCount(defaultNumber(knowledgeMastery.getWrongCount()) + (isCorrect ? 0 : 1));
            knowledgeMastery.setCorrectRate(calculateRate(knowledgeMastery.getCorrectCount(), knowledgeMastery.getTotalCount()));
            knowledgeMastery.setMasteryLevel(resolveMasteryLevel(knowledgeMastery.getCorrectRate(), knowledgeMastery.getTotalCount()));

            if (knowledgeMastery.getId() == null) {
                userKnowledgeMasteryMapper.insert(knowledgeMastery);
            } else {
                userKnowledgeMasteryMapper.updateById(knowledgeMastery);
            }
        }
    }

    private int resolveMasteryLevel(BigDecimal correctRate, Integer totalCount) {
        int answeredCount = defaultNumber(totalCount);
        if (answeredCount <= 0) {
            return 0;
        }

        BigDecimal rate = defaultDecimal(correctRate);
        int minSampleCount = resolveConfiguredMinSampleCount();
        int level1Rate = resolveConfiguredRateLevel1();
        int level2Rate = resolveConfiguredRateLevel2(level1Rate);
        int level3Rate = resolveConfiguredRateLevel3(level2Rate);
        int warmupLevelCap = resolveConfiguredWarmupLevelCap();

        if (answeredCount < minSampleCount) {
            return Math.min(resolveMasteryLevelByRate(rate, level1Rate, level2Rate, level3Rate), warmupLevelCap);
        }

        return resolveMasteryLevelByRate(rate, level1Rate, level2Rate, level3Rate);
    }

    private int resolveMasteryLevelByRate(BigDecimal rate, int level1Rate, int level2Rate, int level3Rate) {
        if (rate.compareTo(BigDecimal.valueOf(level3Rate)) >= 0) {
            return 3;
        }
        if (rate.compareTo(BigDecimal.valueOf(level2Rate)) >= 0) {
            return 2;
        }
        if (rate.compareTo(BigDecimal.valueOf(level1Rate)) >= 0) {
            return 1;
        }
        return 0;
    }

    private int resolveConfiguredMinSampleCount() {
        Integer configured = practiceMasteryProperties.getMinSampleCount();
        return configured == null || configured < 1 ? DEFAULT_MASTERY_MIN_SAMPLE_COUNT : configured;
    }

    private int resolveConfiguredRateLevel1() {
        return clampRate(practiceMasteryProperties.getRateLevel1(), DEFAULT_MASTERY_RATE_LEVEL_1);
    }

    private int resolveConfiguredRateLevel2(int level1Rate) {
        int level2Rate = clampRate(practiceMasteryProperties.getRateLevel2(), DEFAULT_MASTERY_RATE_LEVEL_2);
        return Math.max(level1Rate, level2Rate);
    }

    private int resolveConfiguredRateLevel3(int level2Rate) {
        int level3Rate = clampRate(practiceMasteryProperties.getRateLevel3(), DEFAULT_MASTERY_RATE_LEVEL_3);
        return Math.max(level2Rate, level3Rate);
    }

    private int resolveConfiguredWarmupLevelCap() {
        Integer configured = practiceMasteryProperties.getWarmupLevelCap();
        int warmupLevelCap = configured == null ? DEFAULT_MASTERY_WARMUP_LEVEL_CAP : configured;
        if (warmupLevelCap < 0) {
            return 0;
        }
        return Math.min(warmupLevelCap, 3);
    }

    private int clampRate(Integer configured, int defaultValue) {
        int rate = configured == null ? defaultValue : configured;
        if (rate < 0) {
            return 0;
        }
        return Math.min(rate, 100);
    }

    private BigDecimal calculateRate(Integer correctCount, Integer totalCount) {
        int total = defaultNumber(totalCount);
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(defaultNumber(correctCount))
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
