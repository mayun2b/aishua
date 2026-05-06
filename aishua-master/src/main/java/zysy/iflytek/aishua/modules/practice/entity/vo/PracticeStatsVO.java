package zysy.iflytek.aishua.modules.practice.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PracticeStatsVO {
    private OverviewVO overview = new OverviewVO();

    private List<DailyTrendVO> dailyTrends = new ArrayList<>();

    private List<SubjectStatsVO> subjectStats = new ArrayList<>();

    private List<KnowledgeMasteryVO> knowledgeMasteries = new ArrayList<>();

    private List<KnowledgeMasteryVO> weakPoints = new ArrayList<>();

    private List<PracticeSessionSummaryVO> recentSessions = new ArrayList<>();

    @Data
    public static class OverviewVO {
        private Integer totalCount = 0;

        private Integer correctCount = 0;

        private Integer wrongCount = 0;

        private BigDecimal correctRate = BigDecimal.ZERO;

        private Integer totalTimeCost = 0;

        private Integer todayCount = 0;

        private Integer todayTimeCost = 0;

        private Integer activeDays = 0;

        private Integer continuousDays = 0;

        private Integer maxContinuousDays = 0;

        private Integer wrongQuestionCount = 0;

        private Integer masteredWrongQuestionCount = 0;

        private Integer finishedSessionCount = 0;

        private LocalDate lastExerciseDate;
    }

    @Data
    public static class DailyTrendVO {
        private LocalDate statDate;

        private Integer doCount = 0;

        private Integer correctCount = 0;

        private Integer wrongCount = 0;

        private BigDecimal correctRate = BigDecimal.ZERO;

        private Integer timeCost = 0;
    }

    @Data
    public static class SubjectStatsVO {
        private Long subjectId;

        private String subjectName;

        private Integer totalCount = 0;

        private Integer correctCount = 0;

        private Integer wrongCount = 0;

        private BigDecimal correctRate = BigDecimal.ZERO;

        private Integer totalTimeCost = 0;

        private LocalDate lastPracticeDate;
    }

    @Data
    public static class KnowledgeMasteryVO {
        private Long tagId;

        private String tagName;

        private Long subjectId;

        private String subjectName;

        private Integer totalCount = 0;

        private Integer correctCount = 0;

        private Integer wrongCount = 0;

        private BigDecimal correctRate = BigDecimal.ZERO;

        private Integer masteryLevel = 0;

        private LocalDateTime updateTime;
    }
}
