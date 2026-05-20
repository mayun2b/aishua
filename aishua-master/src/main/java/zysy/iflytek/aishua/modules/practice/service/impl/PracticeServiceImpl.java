package zysy.iflytek.aishua.modules.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.config.properties.PracticeMasteryProperties;
import zysy.iflytek.aishua.modules.directory.entity.TextbookDirectory;
import zysy.iflytek.aishua.modules.directory.mapper.TextbookDirectoryMapper;
import zysy.iflytek.aishua.modules.practice.entity.DailyStats;
import zysy.iflytek.aishua.modules.practice.entity.ExerciseRecord;
import zysy.iflytek.aishua.modules.practice.entity.PracticeSession;
import zysy.iflytek.aishua.modules.practice.entity.UserKnowledgeMastery;
import zysy.iflytek.aishua.modules.practice.entity.UserStats;
import zysy.iflytek.aishua.modules.practice.entity.UserSubjectStats;
import zysy.iflytek.aishua.modules.practice.entity.WrongQuestion;
import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeAnswerItemDTO;
import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeBatchSubmitDTO;
import zysy.iflytek.aishua.modules.practice.entity.dto.PracticeStartDTO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeAnswerResultVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeExerciseRecordVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeQuestionItemVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeQuestionSheetVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeSessionDetailVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeSessionSummaryVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeStatsVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeWrongTrendVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeStartVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeBatchSubmitResultVO;
import zysy.iflytek.aishua.modules.practice.entity.vo.PracticeWrongQuestionVO;
import zysy.iflytek.aishua.modules.practice.mapper.DailyStatsMapper;
import zysy.iflytek.aishua.modules.practice.mapper.ExerciseRecordMapper;
import zysy.iflytek.aishua.modules.practice.mapper.PracticeSessionMapper;
import zysy.iflytek.aishua.modules.practice.mapper.UserKnowledgeMasteryMapper;
import zysy.iflytek.aishua.modules.practice.mapper.UserStatsMapper;
import zysy.iflytek.aishua.modules.practice.mapper.UserSubjectStatsMapper;
import zysy.iflytek.aishua.modules.practice.mapper.WrongQuestionMapper;
import zysy.iflytek.aishua.modules.practice.service.PracticeService;
import zysy.iflytek.aishua.modules.practice.support.AnswerJudgeSupport;
import zysy.iflytek.aishua.modules.practice.support.PracticeModeConstants;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.entity.QuestionTagRelation;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;
import zysy.iflytek.aishua.modules.question.mapper.QuestionTagRelationMapper;
import zysy.iflytek.aishua.modules.subject.entity.Subject;
import zysy.iflytek.aishua.modules.subject.entity.UserSubject;
import zysy.iflytek.aishua.modules.subject.mapper.SubjectMapper;
import zysy.iflytek.aishua.modules.subject.mapper.UserSubjectMapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.entity.vo.ExamTagVO;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PracticeServiceImpl implements PracticeService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int DEFAULT_QUESTION_COUNT = 10;
    private static final int MAX_QUESTION_COUNT = 50;
    private static final int DEFAULT_MASTERY_MIN_SAMPLE_COUNT = 20;
    private static final int DEFAULT_MASTERY_RATE_LEVEL_1 = 60;
    private static final int DEFAULT_MASTERY_RATE_LEVEL_2 = 75;
    private static final int DEFAULT_MASTERY_RATE_LEVEL_3 = 90;
    private static final int DEFAULT_MASTERY_WARMUP_LEVEL_CAP = 2;

    private final PracticeSessionMapper practiceSessionMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final UserStatsMapper userStatsMapper;
    private final UserSubjectStatsMapper userSubjectStatsMapper;
    private final DailyStatsMapper dailyStatsMapper;
    private final UserKnowledgeMasteryMapper userKnowledgeMasteryMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final SubjectMapper subjectMapper;
    private final UserSubjectMapper userSubjectMapper;
    private final ExamTagMapper examTagMapper;
    private final TextbookDirectoryMapper textbookDirectoryMapper;
    private final AnswerJudgeSupport answerJudgeSupport;
    private final PracticeMasteryProperties practiceMasteryProperties;

    public PracticeServiceImpl(
            PracticeSessionMapper practiceSessionMapper,
            ExerciseRecordMapper exerciseRecordMapper,
            WrongQuestionMapper wrongQuestionMapper,
            UserStatsMapper userStatsMapper,
            UserSubjectStatsMapper userSubjectStatsMapper,
            DailyStatsMapper dailyStatsMapper,
            UserKnowledgeMasteryMapper userKnowledgeMasteryMapper,
            QuestionMapper questionMapper,
            QuestionTagRelationMapper questionTagRelationMapper,
            SubjectMapper subjectMapper,
            UserSubjectMapper userSubjectMapper,
            ExamTagMapper examTagMapper,
            TextbookDirectoryMapper textbookDirectoryMapper,
            AnswerJudgeSupport answerJudgeSupport,
            PracticeMasteryProperties practiceMasteryProperties
    ) {
        this.practiceSessionMapper = practiceSessionMapper;
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.wrongQuestionMapper = wrongQuestionMapper;
        this.userStatsMapper = userStatsMapper;
        this.userSubjectStatsMapper = userSubjectStatsMapper;
        this.dailyStatsMapper = dailyStatsMapper;
        this.userKnowledgeMasteryMapper = userKnowledgeMasteryMapper;
        this.questionMapper = questionMapper;
        this.questionTagRelationMapper = questionTagRelationMapper;
        this.subjectMapper = subjectMapper;
        this.userSubjectMapper = userSubjectMapper;
        this.examTagMapper = examTagMapper;
        this.textbookDirectoryMapper = textbookDirectoryMapper;
        this.answerJudgeSupport = answerJudgeSupport;
        this.practiceMasteryProperties = practiceMasteryProperties;
    }

    @Override
    @Transactional
    public PracticeStartVO startPractice(Long userId, PracticeStartDTO practiceStartDTO) {
        Subject subject = requireEnabledSubject(practiceStartDTO.getSubjectId());
        requireJoinedSubject(userId, subject.getId());

        int practiceMode = normalizePracticeMode(practiceStartDTO.getPracticeMode());
        int questionCount = normalizeQuestionCount(practiceStartDTO.getQuestionCount());
        List<Long> tagIds = practiceMode == PracticeModeConstants.KNOWLEDGE_POINT
                ? normalizeTagIds(practiceStartDTO.getTagIds())
                : List.of();
        List<Question> selectedQuestions = selectSessionQuestions(userId, subject.getId(), practiceMode, questionCount, tagIds);
        if (selectedQuestions.isEmpty() && practiceMode == PracticeModeConstants.KNOWLEDGE_POINT) {
            throw new BusinessException("所选知识点暂无可练习题目", 400);
        }
        if (selectedQuestions.isEmpty() && practiceMode == PracticeModeConstants.WRONG_RETRY) {
            throw new BusinessException("当前学科暂无可重练错题", 400);
        }
        if (selectedQuestions.isEmpty()) {
            throw new BusinessException("当前学科暂无可练习题目", 400);
        }

        PracticeSession practiceSession = new PracticeSession();
        practiceSession.setSessionCode(UUID.randomUUID().toString().replace("-", ""));
        practiceSession.setUserId(userId);
        practiceSession.setSubjectId(subject.getId());
        practiceSession.setPracticeMode(practiceMode);
        practiceSession.setQuestionCount(selectedQuestions.size());
        practiceSession.setAnsweredCount(0);
        practiceSession.setCorrectCount(0);
        practiceSession.setWrongCount(0);
        practiceSession.setTotalTimeCost(0);
        practiceSession.setStatus(PracticeModeConstants.STATUS_ONGOING);
        practiceSession.setStartedAt(LocalDateTime.now());
        practiceSessionMapper.insert(practiceSession);

        createSessionPlan(userId, practiceSession, selectedQuestions);

        log.info("练习会话创建成功，userId={}, subjectId={}, sessionId={}, questionCount={}",
                userId, subject.getId(), practiceSession.getId(), selectedQuestions.size());
        return buildStartVO(practiceSession, subject);
    }

    @Override
    public PracticeQuestionSheetVO getPracticeQuestions(Long userId, Long sessionId) {
        PracticeSession practiceSession = requireSession(userId, sessionId);
        List<ExerciseRecord> sessionRecords = listSessionRecords(practiceSession.getId());
        if (sessionRecords.isEmpty()) {
            throw new BusinessException("当前练习会话没有题目数据", 404);
        }

        List<Question> questions = questionMapper.selectBatchIds(sessionRecords.stream()
                .map(ExerciseRecord::getQuestionId)
                .toList());
        Map<Long, Question> questionMap = questions.stream()
                .filter(question -> question != null && !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));

        List<PracticeQuestionItemVO> items = new ArrayList<>();
        for (ExerciseRecord record : sessionRecords) {
            Question question = questionMap.get(record.getQuestionId());
            if (question == null) {
                continue;
            }
            PracticeQuestionItemVO itemVO = new PracticeQuestionItemVO();
            itemVO.setQuestionId(question.getId());
            itemVO.setTitle(question.getTitle());
            itemVO.setContent(question.getContent());
            itemVO.setType(question.getType());
            itemVO.setOptions(question.getOptions());
            itemVO.setImageUrls(question.getImageUrls());
            itemVO.setImageDesc(question.getImageDesc());
            itemVO.setDifficulty(question.getDifficulty());
            items.add(itemVO);
        }

        if (items.isEmpty()) {
            throw new BusinessException("当前练习会话没有有效题目", 404);
        }

        PracticeQuestionSheetVO sheetVO = new PracticeQuestionSheetVO();
        sheetVO.setSessionId(practiceSession.getId());
        sheetVO.setQuestionCount(practiceSession.getQuestionCount());
        sheetVO.setAnsweredCount(practiceSession.getAnsweredCount());
        sheetVO.setFinished(!isSessionOngoing(practiceSession));
        sheetVO.setQuestions(items);
        return sheetVO;
    }

    @Override
    public List<PracticeSessionSummaryVO> listPracticeSessions(Long userId, Long subjectId) {
        validateSubjectFilter(subjectId);

        List<PracticeSession> sessions = practiceSessionMapper.selectList(new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getUserId, userId)
                .eq(subjectId != null, PracticeSession::getSubjectId, subjectId)
                .orderByDesc(PracticeSession::getStartedAt)
                .orderByDesc(PracticeSession::getId));
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Subject> subjectMap = loadSubjectMap(sessions.stream()
                .map(PracticeSession::getSubjectId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<PracticeSessionSummaryVO> result = new ArrayList<>();
        for (PracticeSession session : sessions) {
            Subject subject = subjectMap.get(session.getSubjectId());
            result.add(buildPracticeSessionSummary(session, subject));
        }
        return result;
    }

    @Override
    public PracticeSessionDetailVO getPracticeSessionDetail(Long userId, Long sessionId) {
        PracticeSession session = requireSession(userId, sessionId);
        Subject subject = session.getSubjectId() == null ? null : subjectMapper.selectById(session.getSubjectId());
        List<PracticeExerciseRecordVO> records = listSessionExerciseRecordDetails(session.getId());

        PracticeSessionDetailVO detailVO = new PracticeSessionDetailVO();
        detailVO.setSessionId(session.getId());
        detailVO.setSubjectId(session.getSubjectId());
        detailVO.setSubjectName(subject == null ? null : subject.getName());
        detailVO.setPracticeMode(session.getPracticeMode());
        detailVO.setQuestionCount(session.getQuestionCount());
        detailVO.setAnsweredCount(session.getAnsweredCount());
        detailVO.setCorrectCount(session.getCorrectCount());
        detailVO.setWrongCount(session.getWrongCount());
        detailVO.setTotalTimeCost(session.getTotalTimeCost());
        detailVO.setStatus(session.getStatus());
        detailVO.setCorrectRate(calculateRate(session.getCorrectCount(), session.getAnsweredCount()));
        detailVO.setStartedAt(session.getStartedAt());
        detailVO.setEndedAt(session.getEndedAt());
        detailVO.setRecords(records);
        return detailVO;
    }

    @Override
    public List<PracticeExerciseRecordVO> listExerciseRecords(Long userId, Long subjectId) {
        validateSubjectFilter(subjectId);

        List<Long> sessionIds = listFilteredSessionIds(userId, subjectId);
        if (subjectId != null && sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<ExerciseRecord> queryWrapper = new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, userId)
                .isNotNull(ExerciseRecord::getUserAnswer)
                .orderByDesc(ExerciseRecord::getExerciseTime)
                .orderByDesc(ExerciseRecord::getId);
        if (subjectId != null) {
            queryWrapper.in(ExerciseRecord::getSessionRefId, sessionIds);
        }

        List<ExerciseRecord> records = exerciseRecordMapper.selectList(queryWrapper);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, PracticeSession> sessionMap = loadSessionMap(records.stream()
                .map(ExerciseRecord::getSessionRefId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        Map<Long, Question> questionMap = loadQuestionMap(records.stream()
                .map(ExerciseRecord::getQuestionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        Map<Long, Subject> subjectMap = loadSubjectMap(sessionMap.values().stream()
                .map(PracticeSession::getSubjectId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<PracticeExerciseRecordVO> result = new ArrayList<>();
        for (ExerciseRecord record : records) {
            PracticeSession session = sessionMap.get(record.getSessionRefId());
            Question question = questionMap.get(record.getQuestionId());
            Subject subject = session == null ? null : subjectMap.get(session.getSubjectId());

            PracticeExerciseRecordVO recordVO = new PracticeExerciseRecordVO();
            recordVO.setRecordId(record.getId());
            recordVO.setSessionId(record.getSessionRefId());
            recordVO.setPracticeMode(session == null ? record.getExerciseMode() : session.getPracticeMode());
            recordVO.setSubjectId(session == null ? null : session.getSubjectId());
            recordVO.setSubjectName(subject == null ? null : subject.getName());
            recordVO.setQuestionId(record.getQuestionId());
            recordVO.setQuestionTitle(question == null ? "题目已删除" : question.getTitle());
            recordVO.setQuestionType(question == null ? null : question.getType());
            recordVO.setDifficulty(question == null ? null : question.getDifficulty());
            recordVO.setUserAnswer(record.getUserAnswer());
            recordVO.setCorrectAnswer(question == null ? null : question.getAnswer());
            recordVO.setIsCorrect(record.getIsCorrect());
            recordVO.setTimeCost(record.getTimeCost());
            recordVO.setExerciseTime(record.getExerciseTime());
            result.add(recordVO);
        }
        return result;
    }

    @Override
    public List<PracticeWrongQuestionVO> listWrongQuestions(Long userId, Long subjectId, Long directoryId, Integer masterStatus) {
        validateSubjectFilter(subjectId);
        validateDirectoryFilter(directoryId, subjectId);
        validateMasterStatusFilter(masterStatus);

        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(subjectId != null, WrongQuestion::getSubjectId, subjectId)
                .eq(directoryId != null, WrongQuestion::getDirectoryId, directoryId)
                .eq(masterStatus != null, WrongQuestion::getMasterStatus, masterStatus)
                .orderByAsc(WrongQuestion::getMasterStatus)
                .orderByDesc(WrongQuestion::getWrongCount)
                .orderByDesc(WrongQuestion::getLastWrongTime)
                .orderByDesc(WrongQuestion::getId));
        if (wrongQuestions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Subject> subjectMap = loadSubjectMap(wrongQuestions.stream()
                .map(WrongQuestion::getSubjectId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        Map<Long, Question> questionMap = loadQuestionMap(wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        Map<Long, TextbookDirectory> directoryMap = loadDirectoryMap(wrongQuestions.stream()
                .map(WrongQuestion::getDirectoryId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<PracticeWrongQuestionVO> result = new ArrayList<>();
        for (WrongQuestion wrongQuestion : wrongQuestions) {
            result.add(toWrongQuestionVO(wrongQuestion, subjectMap, questionMap, directoryMap));
        }
        return result;
    }

    @Override
    @Transactional
    public PracticeWrongQuestionVO updateWrongQuestionMasterStatus(Long userId, Long wrongQuestionId, Integer masterStatus) {
        int normalizedMasterStatus = normalizeMasterStatus(masterStatus);
        WrongQuestion wrongQuestion = requireWrongQuestion(userId, wrongQuestionId);

        wrongQuestion.setMasterStatus(normalizedMasterStatus);
        wrongQuestionMapper.updateById(wrongQuestion);

        Map<Long, Subject> subjectMap = loadSubjectMap(wrongQuestion.getSubjectId() == null ? List.of() : List.of(wrongQuestion.getSubjectId()));
        Map<Long, Question> questionMap = loadQuestionMap(wrongQuestion.getQuestionId() == null ? List.of() : List.of(wrongQuestion.getQuestionId()));
        Map<Long, TextbookDirectory> directoryMap = loadDirectoryMap(wrongQuestion.getDirectoryId() == null ? List.of() : List.of(wrongQuestion.getDirectoryId()));
        return toWrongQuestionVO(wrongQuestion, subjectMap, questionMap, directoryMap);
    }

    @Override
    public List<PracticeWrongTrendVO> getWrongQuestionTrends(Long userId, Long subjectId, Long directoryId, Integer days) {
        validateSubjectFilter(subjectId);
        validateDirectoryFilter(directoryId, subjectId);

        int statsDays = normalizeStatsDays(days);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(statsDays - 1L);

        LambdaQueryWrapper<ExerciseRecord> queryWrapper = new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, userId)
                .eq(ExerciseRecord::getIsCorrect, 0)
                .isNotNull(ExerciseRecord::getExerciseTime)
                .ge(ExerciseRecord::getExerciseTime, startDate.atStartOfDay())
                .orderByAsc(ExerciseRecord::getExerciseTime)
                .orderByAsc(ExerciseRecord::getId);

        if (subjectId != null) {
            List<Long> sessionIds = listFilteredSessionIds(userId, subjectId);
            if (sessionIds.isEmpty()) {
                return buildEmptyWrongTrends(startDate, today);
            }
            queryWrapper.in(ExerciseRecord::getSessionRefId, sessionIds);
        }

        List<ExerciseRecord> wrongAnswerRecords = exerciseRecordMapper.selectList(queryWrapper);
        if (wrongAnswerRecords.isEmpty()) {
            return buildEmptyWrongTrends(startDate, today);
        }

        List<ExerciseRecord> filteredRecords = wrongAnswerRecords;
        if (directoryId != null) {
            Map<Long, Question> questionMap = loadQuestionMap(wrongAnswerRecords.stream()
                    .map(ExerciseRecord::getQuestionId)
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .toList());
            filteredRecords = wrongAnswerRecords.stream()
                    .filter(record -> {
                        Question question = questionMap.get(record.getQuestionId());
                        return question != null && directoryId.equals(question.getDirectoryId());
                    })
                    .toList();
            if (filteredRecords.isEmpty()) {
                return buildEmptyWrongTrends(startDate, today);
            }
        }

        Map<LocalDate, Integer> wrongAnswerCountMap = new LinkedHashMap<>();
        Map<LocalDate, Set<Long>> uniqueQuestionMap = new LinkedHashMap<>();
        for (ExerciseRecord record : filteredRecords) {
            if (record.getExerciseTime() == null) {
                continue;
            }
            LocalDate statDate = record.getExerciseTime().toLocalDate();
            wrongAnswerCountMap.merge(statDate, 1, Integer::sum);
            uniqueQuestionMap.computeIfAbsent(statDate, key -> new HashSet<>()).add(record.getQuestionId());
        }

        List<PracticeWrongTrendVO> result = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(today)) {
            PracticeWrongTrendVO trendVO = new PracticeWrongTrendVO();
            trendVO.setStatDate(cursor);
            trendVO.setWrongAnswerCount(wrongAnswerCountMap.getOrDefault(cursor, 0));
            trendVO.setUniqueWrongQuestionCount(uniqueQuestionMap.getOrDefault(cursor, Collections.emptySet()).size());
            result.add(trendVO);
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    @Override
    public PracticeStatsVO getPracticeStats(Long userId, Integer days) {
        int statsDays = normalizeStatsDays(days);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(statsDays - 1L);

        UserStats userStats = userStatsMapper.selectOne(new LambdaQueryWrapper<UserStats>()
                .eq(UserStats::getUserId, userId)
                .last("LIMIT 1"));
        List<DailyStats> allDailyStats = dailyStatsMapper.selectList(new LambdaQueryWrapper<DailyStats>()
                .eq(DailyStats::getUserId, userId)
                .orderByDesc(DailyStats::getStatDate));
        List<DailyStats> windowDailyStats = allDailyStats.stream()
                .filter(stats -> stats.getStatDate() != null && !stats.getStatDate().isBefore(startDate))
                .toList();
        List<UserSubjectStats> subjectStats = userSubjectStatsMapper.selectList(new LambdaQueryWrapper<UserSubjectStats>()
                .eq(UserSubjectStats::getUserId, userId)
                .orderByDesc(UserSubjectStats::getTotalCount)
                .orderByDesc(UserSubjectStats::getLastPracticeDate));
        List<UserKnowledgeMastery> knowledgeMasteries = userKnowledgeMasteryMapper.selectList(new LambdaQueryWrapper<UserKnowledgeMastery>()
                .eq(UserKnowledgeMastery::getUserId, userId)
                .orderByAsc(UserKnowledgeMastery::getMasteryLevel)
                .orderByDesc(UserKnowledgeMastery::getWrongCount)
                .orderByDesc(UserKnowledgeMastery::getTotalCount));

        PracticeStatsVO statsVO = new PracticeStatsVO();
        statsVO.setDailyTrends(buildDailyTrends(startDate, today, windowDailyStats));
        statsVO.setSubjectStats(buildSubjectStats(subjectStats));
        statsVO.setKnowledgeMasteries(buildKnowledgeMasteries(knowledgeMasteries, 30));
        statsVO.setWeakPoints(buildWeakPoints(knowledgeMasteries, 8));
        statsVO.setRecentSessions(listRecentPracticeSessions(userId, 5));
        statsVO.setOverview(buildOverview(userId, userStats, allDailyStats, subjectStats, today));
        return statsVO;
    }

    @Override
    public List<ExamTagVO> listPracticeTags(Long userId, Long subjectId) {
        Subject subject = requireEnabledSubject(subjectId);
        requireJoinedSubject(userId, subject.getId());

        return examTagMapper.selectList(new LambdaQueryWrapper<ExamTag>()
                        .eq(ExamTag::getSubjectId, subject.getId())
                        .orderByAsc(ExamTag::getName)
                        .orderByAsc(ExamTag::getId))
                .stream()
                .map(tag -> toExamTagVO(tag, subject.getName()))
                .toList();
    }

    @Override
    @Transactional
    public PracticeBatchSubmitResultVO submitPractice(Long userId, Long sessionId, PracticeBatchSubmitDTO practiceBatchSubmitDTO) {
        PracticeSession practiceSession = requireSession(userId, sessionId);
        if (!isSessionOngoing(practiceSession)) {
            throw new BusinessException("当前练习已结束，请重新开始", 400);
        }

        List<ExerciseRecord> sessionRecords = listSessionRecords(practiceSession.getId());
        if (sessionRecords.isEmpty()) {
            throw new BusinessException("当前练习没有题目可提交", 400);
        }
        if (sessionRecords.stream().anyMatch(record -> record.getUserAnswer() != null)) {
            throw new BusinessException("当前练习已提交，请勿重复提交", 400);
        }

        Map<Long, PracticeAnswerItemDTO> answerMap = toAnswerMap(practiceBatchSubmitDTO.getAnswers());
        Map<Long, Question> questionMap = questionMapper.selectBatchIds(sessionRecords.stream()
                        .map(ExerciseRecord::getQuestionId)
                        .toList())
                .stream()
                .filter(question -> question != null && !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));

        int correctCount = 0;
        int wrongCount = 0;
        int totalTimeCost = 0;
        List<PracticeAnswerResultVO> results = new ArrayList<>();
        LocalDateTime submitTime = LocalDateTime.now();

        for (ExerciseRecord record : sessionRecords) {
            Question question = questionMap.get(record.getQuestionId());
            if (question == null) {
                throw new BusinessException("练习题目不存在或已失效", 400);
            }

            PracticeAnswerItemDTO answerItem = answerMap.get(question.getId());
            String submittedAnswer = normalizeSubmittedAnswer(answerItem == null ? null : answerItem.getUserAnswer());
            int timeCost = normalizeTimeCost(answerItem == null ? null : answerItem.getTimeCost());
            boolean isCorrect = answerJudgeSupport.isCorrect(question.getType(), question.getAnswer(), submittedAnswer);

            record.setUserAnswer(submittedAnswer);
            record.setIsCorrect(isCorrect ? 1 : 0);
            record.setTimeCost(timeCost);
            record.setExerciseTime(submitTime);
            exerciseRecordMapper.updateById(record);

            updateQuestionStats(question, isCorrect);
            updateUserStats(userId, isCorrect);
            updateUserSubjectStats(userId, practiceSession.getSubjectId(), isCorrect, timeCost);
            updateDailyStats(userId, isCorrect, timeCost);

            TagSnapshot tagSnapshot = loadTagSnapshot(question.getId());
            updateKnowledgeMastery(userId, question.getSubjectId(), tagSnapshot.tagIds(), isCorrect);
            if (!isCorrect) {
                upsertWrongQuestion(userId, question, tagSnapshot.tagNames(), submitTime);
            }

            correctCount += isCorrect ? 1 : 0;
            wrongCount += isCorrect ? 0 : 1;
            totalTimeCost += timeCost;
            results.add(buildAnswerResult(question, submittedAnswer, isCorrect, timeCost));
        }

        practiceSession.setAnsweredCount(sessionRecords.size());
        practiceSession.setCorrectCount(correctCount);
        practiceSession.setWrongCount(wrongCount);
        practiceSession.setTotalTimeCost(totalTimeCost);
        practiceSession.setStatus(PracticeModeConstants.STATUS_FINISHED);
        practiceSession.setEndedAt(submitTime);
        practiceSessionMapper.updateById(practiceSession);
        updateUserSubjectLastPracticeTime(userId, practiceSession.getSubjectId(), submitTime);

        log.info("练习批量提交完成，userId={}, sessionId={}, answeredCount={}, correctCount={}",
                userId, practiceSession.getId(), sessionRecords.size(), correctCount);

        PracticeBatchSubmitResultVO resultVO = new PracticeBatchSubmitResultVO();
        resultVO.setSessionId(practiceSession.getId());
        resultVO.setQuestionCount(practiceSession.getQuestionCount());
        resultVO.setAnsweredCount(sessionRecords.size());
        resultVO.setCorrectCount(correctCount);
        resultVO.setWrongCount(wrongCount);
        resultVO.setTotalTimeCost(totalTimeCost);
        resultVO.setCorrectRate(calculateRate(correctCount, sessionRecords.size()));
        resultVO.setFinished(true);
        resultVO.setResults(results);
        return resultVO;
    }

    private void createSessionPlan(Long userId, PracticeSession practiceSession, List<Question> selectedQuestions) {
        for (Question question : selectedQuestions) {
            ExerciseRecord record = new ExerciseRecord();
            record.setUserId(userId);
            record.setQuestionId(question.getId());
            record.setSessionRefId(practiceSession.getId());
            record.setExerciseMode(practiceSession.getPracticeMode());
            record.setUserAnswer(null);
            record.setIsCorrect(null);
            record.setTimeCost(0);
            record.setExerciseTime(null);
            exerciseRecordMapper.insert(record);
        }
    }

    private PracticeStartVO buildStartVO(PracticeSession practiceSession, Subject subject) {
        PracticeStartVO practiceStartVO = new PracticeStartVO();
        practiceStartVO.setSessionId(practiceSession.getId());
        practiceStartVO.setSessionCode(practiceSession.getSessionCode());
        practiceStartVO.setSubjectId(practiceSession.getSubjectId());
        practiceStartVO.setSubjectName(subject.getName());
        practiceStartVO.setPracticeMode(practiceSession.getPracticeMode());
        practiceStartVO.setQuestionCount(practiceSession.getQuestionCount());
        practiceStartVO.setAnsweredCount(practiceSession.getAnsweredCount());
        practiceStartVO.setCorrectCount(practiceSession.getCorrectCount());
        practiceStartVO.setWrongCount(practiceSession.getWrongCount());
        practiceStartVO.setStatus(practiceSession.getStatus());
        practiceStartVO.setStartedAt(practiceSession.getStartedAt());
        return practiceStartVO;
    }

    private PracticeSessionSummaryVO buildPracticeSessionSummary(PracticeSession session, Subject subject) {
        PracticeSessionSummaryVO summaryVO = new PracticeSessionSummaryVO();
        summaryVO.setSessionId(session.getId());
        summaryVO.setSubjectId(session.getSubjectId());
        summaryVO.setSubjectName(subject == null ? null : subject.getName());
        summaryVO.setPracticeMode(session.getPracticeMode());
        summaryVO.setQuestionCount(session.getQuestionCount());
        summaryVO.setAnsweredCount(session.getAnsweredCount());
        summaryVO.setCorrectCount(session.getCorrectCount());
        summaryVO.setWrongCount(session.getWrongCount());
        summaryVO.setTotalTimeCost(session.getTotalTimeCost());
        summaryVO.setStatus(session.getStatus());
        summaryVO.setCorrectRate(calculateRate(session.getCorrectCount(), session.getAnsweredCount()));
        summaryVO.setStartedAt(session.getStartedAt());
        summaryVO.setEndedAt(session.getEndedAt());
        return summaryVO;
    }

    private PracticeAnswerResultVO buildAnswerResult(Question question, String submittedAnswer, boolean isCorrect, int timeCost) {
        PracticeAnswerResultVO resultVO = new PracticeAnswerResultVO();
        resultVO.setQuestionId(question.getId());
        resultVO.setQuestionTitle(question.getTitle());
        resultVO.setUserAnswer(submittedAnswer);
        resultVO.setCorrectAnswer(question.getAnswer());
        resultVO.setAnalysis(question.getAnalysis());
        resultVO.setIsCorrect(isCorrect ? 1 : 0);
        resultVO.setTimeCost(timeCost);
        return resultVO;
    }

    private PracticeStatsVO.OverviewVO buildOverview(
            Long userId,
            UserStats userStats,
            List<DailyStats> dailyStatsList,
            List<UserSubjectStats> subjectStatsList,
            LocalDate today
    ) {
        PracticeStatsVO.OverviewVO overviewVO = new PracticeStatsVO.OverviewVO();
        if (userStats != null) {
            overviewVO.setTotalCount(defaultNumber(userStats.getTotalCount()));
            overviewVO.setCorrectCount(defaultNumber(userStats.getCorrectCount()));
            overviewVO.setWrongCount(defaultNumber(userStats.getWrongCount()));
            overviewVO.setCorrectRate(defaultDecimal(userStats.getCorrectRate()));
            overviewVO.setLastExerciseDate(userStats.getLastExerciseDate());
        }

        int totalTimeCost = subjectStatsList.stream()
                .map(UserSubjectStats::getTotalTimeCost)
                .mapToInt(this::defaultNumber)
                .sum();
        overviewVO.setTotalTimeCost(totalTimeCost);
        overviewVO.setActiveDays((int) dailyStatsList.stream()
                .filter(stats -> defaultNumber(stats.getDoCount()) > 0)
                .count());
        overviewVO.setContinuousDays(calculateContinuousDays(dailyStatsList, today));
        overviewVO.setMaxContinuousDays(calculateMaxContinuousDays(dailyStatsList));

        DailyStats todayStats = dailyStatsList.stream()
                .filter(stats -> today.equals(stats.getStatDate()))
                .findFirst()
                .orElse(null);
        if (todayStats != null) {
            overviewVO.setTodayCount(defaultNumber(todayStats.getDoCount()));
            overviewVO.setTodayTimeCost(defaultNumber(todayStats.getTimeCost()));
        }

        Long wrongQuestionCount = wrongQuestionMapper.selectCount(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId));
        Long masteredWrongQuestionCount = wrongQuestionMapper.selectCount(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getMasterStatus, 1));
        Long finishedSessionCount = practiceSessionMapper.selectCount(new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getUserId, userId)
                .eq(PracticeSession::getStatus, PracticeModeConstants.STATUS_FINISHED));

        overviewVO.setWrongQuestionCount(toInt(wrongQuestionCount));
        overviewVO.setMasteredWrongQuestionCount(toInt(masteredWrongQuestionCount));
        overviewVO.setFinishedSessionCount(toInt(finishedSessionCount));
        return overviewVO;
    }

    private List<PracticeStatsVO.DailyTrendVO> buildDailyTrends(LocalDate startDate, LocalDate today, List<DailyStats> dailyStatsList) {
        Map<LocalDate, DailyStats> dailyStatsMap = dailyStatsList.stream()
                .filter(stats -> stats.getStatDate() != null)
                .collect(Collectors.toMap(DailyStats::getStatDate, Function.identity(), (left, right) -> left));

        List<PracticeStatsVO.DailyTrendVO> result = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(today)) {
            DailyStats dailyStats = dailyStatsMap.get(cursor);
            PracticeStatsVO.DailyTrendVO trendVO = new PracticeStatsVO.DailyTrendVO();
            trendVO.setStatDate(cursor);
            if (dailyStats != null) {
                int doCount = defaultNumber(dailyStats.getDoCount());
                int correctCount = defaultNumber(dailyStats.getCorrectCount());
                trendVO.setDoCount(doCount);
                trendVO.setCorrectCount(correctCount);
                trendVO.setWrongCount(Math.max(doCount - correctCount, 0));
                trendVO.setCorrectRate(calculateRate(correctCount, doCount));
                trendVO.setTimeCost(defaultNumber(dailyStats.getTimeCost()));
            }
            result.add(trendVO);
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    private List<PracticeStatsVO.SubjectStatsVO> buildSubjectStats(List<UserSubjectStats> subjectStatsList) {
        Map<Long, Subject> subjectMap = loadSubjectMap(subjectStatsList.stream()
                .map(UserSubjectStats::getSubjectId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<PracticeStatsVO.SubjectStatsVO> result = new ArrayList<>();
        for (UserSubjectStats subjectStats : subjectStatsList) {
            Subject subject = subjectMap.get(subjectStats.getSubjectId());
            PracticeStatsVO.SubjectStatsVO subjectStatsVO = new PracticeStatsVO.SubjectStatsVO();
            subjectStatsVO.setSubjectId(subjectStats.getSubjectId());
            subjectStatsVO.setSubjectName(subject == null ? "已删除学科" : subject.getName());
            subjectStatsVO.setTotalCount(defaultNumber(subjectStats.getTotalCount()));
            subjectStatsVO.setCorrectCount(defaultNumber(subjectStats.getCorrectCount()));
            subjectStatsVO.setWrongCount(defaultNumber(subjectStats.getWrongCount()));
            subjectStatsVO.setCorrectRate(defaultDecimal(subjectStats.getCorrectRate()));
            subjectStatsVO.setTotalTimeCost(defaultNumber(subjectStats.getTotalTimeCost()));
            subjectStatsVO.setLastPracticeDate(subjectStats.getLastPracticeDate());
            result.add(subjectStatsVO);
        }
        return result;
    }

    private List<PracticeStatsVO.KnowledgeMasteryVO> buildKnowledgeMasteries(List<UserKnowledgeMastery> knowledgeMasteries, int limit) {
        if (knowledgeMasteries.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Subject> subjectMap = loadSubjectMap(knowledgeMasteries.stream()
                .map(UserKnowledgeMastery::getSubjectId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());
        Map<Long, ExamTag> tagMap = loadTagMap(knowledgeMasteries.stream()
                .map(UserKnowledgeMastery::getTagId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<PracticeStatsVO.KnowledgeMasteryVO> result = new ArrayList<>();
        for (UserKnowledgeMastery mastery : knowledgeMasteries.stream().limit(limit).toList()) {
            result.add(toKnowledgeMasteryVO(mastery, subjectMap, tagMap));
        }
        return result;
    }

    private List<PracticeStatsVO.KnowledgeMasteryVO> buildWeakPoints(List<UserKnowledgeMastery> knowledgeMasteries, int limit) {
        if (knowledgeMasteries.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserKnowledgeMastery> weakMasteries = knowledgeMasteries.stream()
                .filter(mastery -> defaultNumber(mastery.getTotalCount()) > 0)
                .sorted((left, right) -> {
                    int levelCompare = Integer.compare(defaultNumber(left.getMasteryLevel()), defaultNumber(right.getMasteryLevel()));
                    if (levelCompare != 0) {
                        return levelCompare;
                    }
                    int wrongCompare = Integer.compare(defaultNumber(right.getWrongCount()), defaultNumber(left.getWrongCount()));
                    if (wrongCompare != 0) {
                        return wrongCompare;
                    }
                    return defaultDecimal(left.getCorrectRate()).compareTo(defaultDecimal(right.getCorrectRate()));
                })
                .limit(limit)
                .toList();
        return buildKnowledgeMasteries(weakMasteries, limit);
    }

    private PracticeStatsVO.KnowledgeMasteryVO toKnowledgeMasteryVO(
            UserKnowledgeMastery mastery,
            Map<Long, Subject> subjectMap,
            Map<Long, ExamTag> tagMap
    ) {
        Subject subject = subjectMap.get(mastery.getSubjectId());
        ExamTag tag = tagMap.get(mastery.getTagId());

        PracticeStatsVO.KnowledgeMasteryVO masteryVO = new PracticeStatsVO.KnowledgeMasteryVO();
        masteryVO.setTagId(mastery.getTagId());
        masteryVO.setTagName(tag == null ? "已删除考点" : tag.getName());
        masteryVO.setSubjectId(mastery.getSubjectId());
        masteryVO.setSubjectName(subject == null ? "已删除学科" : subject.getName());
        masteryVO.setTotalCount(defaultNumber(mastery.getTotalCount()));
        masteryVO.setCorrectCount(defaultNumber(mastery.getCorrectCount()));
        masteryVO.setWrongCount(defaultNumber(mastery.getWrongCount()));
        masteryVO.setCorrectRate(defaultDecimal(mastery.getCorrectRate()));
        masteryVO.setMasteryLevel(defaultNumber(mastery.getMasteryLevel()));
        masteryVO.setUpdateTime(mastery.getUpdateTime());
        return masteryVO;
    }

    private List<PracticeSessionSummaryVO> listRecentPracticeSessions(Long userId, int limit) {
        List<PracticeSession> sessions = practiceSessionMapper.selectList(new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getUserId, userId)
                .orderByDesc(PracticeSession::getStartedAt)
                .orderByDesc(PracticeSession::getId)
                .last("LIMIT " + limit));
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Subject> subjectMap = loadSubjectMap(sessions.stream()
                .map(PracticeSession::getSubjectId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<PracticeSessionSummaryVO> result = new ArrayList<>();
        for (PracticeSession session : sessions) {
            result.add(buildPracticeSessionSummary(session, subjectMap.get(session.getSubjectId())));
        }
        return result;
    }

    private int calculateContinuousDays(List<DailyStats> dailyStatsList, LocalDate today) {
        Set<LocalDate> activeDates = toActiveDateSet(dailyStatsList);
        int continuousDays = 0;
        LocalDate cursor = today;
        while (activeDates.contains(cursor)) {
            continuousDays++;
            cursor = cursor.minusDays(1);
        }
        return continuousDays;
    }

    private int calculateMaxContinuousDays(List<DailyStats> dailyStatsList) {
        List<LocalDate> activeDates = new ArrayList<>(toActiveDateSet(dailyStatsList));
        Collections.sort(activeDates);

        int maxContinuousDays = 0;
        int currentContinuousDays = 0;
        LocalDate previousDate = null;
        for (LocalDate activeDate : activeDates) {
            if (previousDate != null && activeDate.equals(previousDate.plusDays(1))) {
                currentContinuousDays++;
            } else {
                currentContinuousDays = 1;
            }
            maxContinuousDays = Math.max(maxContinuousDays, currentContinuousDays);
            previousDate = activeDate;
        }
        return maxContinuousDays;
    }

    private Set<LocalDate> toActiveDateSet(List<DailyStats> dailyStatsList) {
        Set<LocalDate> activeDates = new HashSet<>();
        for (DailyStats dailyStats : dailyStatsList) {
            if (dailyStats.getStatDate() != null && defaultNumber(dailyStats.getDoCount()) > 0) {
                activeDates.add(dailyStats.getStatDate());
            }
        }
        return activeDates;
    }

    private Subject requireEnabledSubject(Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            throw new BusinessException("学科ID不合法", 400);
        }
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || Integer.valueOf(1).equals(subject.getDeleted())) {
            throw new BusinessException("学科不存在", 404);
        }
        if (!Integer.valueOf(1).equals(subject.getIsEnabled())) {
            throw new BusinessException("当前学科已停用，暂不支持练习", 400);
        }
        return subject;
    }

    private UserSubject requireJoinedSubject(Long userId, Long subjectId) {
        UserSubject userSubject = userSubjectMapper.selectOne(new LambdaQueryWrapper<UserSubject>()
                .eq(UserSubject::getUserId, userId)
                .eq(UserSubject::getSubjectId, subjectId)
                .last("LIMIT 1"));
        if (userSubject == null || Integer.valueOf(1).equals(userSubject.getDeleted())) {
            throw new BusinessException("请先加入该学科后再开始练习", 400);
        }
        if (!Integer.valueOf(1).equals(userSubject.getStatus())) {
            throw new BusinessException("当前学科学习状态不可用，请先恢复后再练习", 400);
        }
        return userSubject;
    }

    private PracticeSession requireSession(Long userId, Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new BusinessException("练习会话ID不合法", 400);
        }
        PracticeSession practiceSession = practiceSessionMapper.selectById(sessionId);
        if (practiceSession == null) {
            throw new BusinessException("练习会话不存在", 404);
        }
        if (!userId.equals(practiceSession.getUserId())) {
            throw new BusinessException("无权访问该练习会话", 403);
        }
        return practiceSession;
    }

    private List<Question> selectSessionQuestions(Long userId, Long subjectId, int practiceMode, int questionCount, List<Long> tagIds) {
        if (practiceMode == PracticeModeConstants.KNOWLEDGE_POINT) {
            return selectTaggedQuestions(subjectId, tagIds, questionCount);
        }
        if (practiceMode == PracticeModeConstants.WRONG_RETRY) {
            return selectWrongRetryQuestions(userId, subjectId, questionCount);
        }

        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getSubjectId, subjectId)
                .orderByAsc(Question::getId));
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Question> selectedQuestions = new ArrayList<>(questions);
        if (practiceMode == PracticeModeConstants.RANDOM) {
            Collections.shuffle(selectedQuestions);
        }

        int limit = Math.min(selectedQuestions.size(), questionCount);
        return new ArrayList<>(selectedQuestions.subList(0, limit));
    }

    private List<Question> selectWrongRetryQuestions(Long userId, Long subjectId, int questionCount) {
        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getSubjectId, subjectId)
                .orderByAsc(WrongQuestion::getMasterStatus)
                .orderByDesc(WrongQuestion::getWrongCount)
                .orderByDesc(WrongQuestion::getLastWrongTime)
                .orderByDesc(WrongQuestion::getId));
        if (wrongQuestions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = loadQuestionMap(wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<Question> selected = new ArrayList<>();
        for (WrongQuestion wrongQuestion : wrongQuestions) {
            Question question = questionMap.get(wrongQuestion.getQuestionId());
            if (question == null || Integer.valueOf(1).equals(question.getDeleted())) {
                continue;
            }
            selected.add(question);
            if (selected.size() >= questionCount) {
                break;
            }
        }
        return selected;
    }

    private List<Question> selectTaggedQuestions(Long subjectId, List<Long> tagIds, int questionCount) {
        ensureTagsBelongToSubject(subjectId, tagIds);

        List<QuestionTagRelation> relations = questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                .in(QuestionTagRelation::getTagId, tagIds));
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = relations.stream()
                .map(QuestionTagRelation::getQuestionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (questionIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = questionMapper.selectBatchIds(questionIds).stream()
                .filter(question -> question != null)
                .filter(question -> subjectId.equals(question.getSubjectId()))
                .filter(question -> !Integer.valueOf(1).equals(question.getDeleted()))
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));

        List<Question> selectedQuestions = new ArrayList<>();
        for (Long questionId : questionIds) {
            Question question = questionMap.get(questionId);
            if (question != null) {
                selectedQuestions.add(question);
            }
        }

        Collections.shuffle(selectedQuestions);
        int limit = Math.min(selectedQuestions.size(), questionCount);
        return new ArrayList<>(selectedQuestions.subList(0, limit));
    }

    private void ensureTagsBelongToSubject(Long subjectId, List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            throw new BusinessException("请选择知识点", 400);
        }

        List<ExamTag> tags = examTagMapper.selectBatchIds(tagIds);
        Set<Long> matchedTagIds = tags.stream()
                .filter(tag -> tag != null && subjectId.equals(tag.getSubjectId()))
                .map(ExamTag::getId)
                .collect(Collectors.toSet());
        if (matchedTagIds.size() != tagIds.size()) {
            throw new BusinessException("存在不属于当前学科的知识点", 400);
        }
    }

    private List<ExerciseRecord> listSessionRecords(Long sessionId) {
        return exerciseRecordMapper.selectList(new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getSessionRefId, sessionId)
                .orderByAsc(ExerciseRecord::getId));
    }

    private List<PracticeExerciseRecordVO> listSessionExerciseRecordDetails(Long sessionId) {
        List<ExerciseRecord> records = listSessionRecords(sessionId).stream()
                .filter(record -> record.getUserAnswer() != null)
                .toList();
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = loadQuestionMap(records.stream()
                .map(ExerciseRecord::getQuestionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList());

        List<PracticeExerciseRecordVO> result = new ArrayList<>();
        for (ExerciseRecord record : records) {
            Question question = questionMap.get(record.getQuestionId());
            PracticeExerciseRecordVO recordVO = new PracticeExerciseRecordVO();
            recordVO.setRecordId(record.getId());
            recordVO.setSessionId(record.getSessionRefId());
            recordVO.setPracticeMode(record.getExerciseMode());
            recordVO.setQuestionId(record.getQuestionId());
            recordVO.setQuestionTitle(question == null ? "题目已删除" : question.getTitle());
            recordVO.setQuestionType(question == null ? null : question.getType());
            recordVO.setDifficulty(question == null ? null : question.getDifficulty());
            recordVO.setUserAnswer(record.getUserAnswer());
            recordVO.setCorrectAnswer(question == null ? null : question.getAnswer());
            recordVO.setIsCorrect(record.getIsCorrect());
            recordVO.setTimeCost(record.getTimeCost());
            recordVO.setExerciseTime(record.getExerciseTime());
            result.add(recordVO);
        }
        return result;
    }

    private List<Long> listFilteredSessionIds(Long userId, Long subjectId) {
        LambdaQueryWrapper<PracticeSession> queryWrapper = new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getUserId, userId);
        if (subjectId != null) {
            queryWrapper.eq(PracticeSession::getSubjectId, subjectId);
        }
        return practiceSessionMapper.selectList(queryWrapper).stream()
                .map(PracticeSession::getId)
                .filter(id -> id != null && id > 0)
                .toList();
    }

    private Map<Long, PracticeSession> loadSessionMap(List<Long> sessionIds) {
        if (sessionIds.isEmpty()) {
            return Map.of();
        }
        return practiceSessionMapper.selectBatchIds(sessionIds).stream()
                .filter(session -> session != null)
                .collect(Collectors.toMap(PracticeSession::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Question> loadQuestionMap(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .filter(question -> question != null)
                .collect(Collectors.toMap(Question::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Subject> loadSubjectMap(List<Long> subjectIds) {
        if (subjectIds.isEmpty()) {
            return Map.of();
        }
        return subjectMapper.selectBatchIds(subjectIds).stream()
                .filter(subject -> subject != null)
                .collect(Collectors.toMap(Subject::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, TextbookDirectory> loadDirectoryMap(List<Long> directoryIds) {
        if (directoryIds.isEmpty()) {
            return Map.of();
        }
        return textbookDirectoryMapper.selectBatchIds(directoryIds).stream()
                .filter(directory -> directory != null)
                .collect(Collectors.toMap(TextbookDirectory::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, ExamTag> loadTagMap(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return Map.of();
        }
        return examTagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> tag != null)
                .collect(Collectors.toMap(ExamTag::getId, Function.identity(), (left, right) -> left));
    }

    private ExamTagVO toExamTagVO(ExamTag tag, String subjectName) {
        ExamTagVO tagVO = new ExamTagVO();
        tagVO.setId(tag.getId());
        tagVO.setName(tag.getName());
        tagVO.setSubjectId(tag.getSubjectId());
        tagVO.setSubjectName(subjectName);
        tagVO.setTag(tag.getTag());
        tagVO.setCreateTime(tag.getCreateTime());
        tagVO.setUpdateTime(tag.getUpdateTime());
        return tagVO;
    }

    private PracticeWrongQuestionVO toWrongQuestionVO(
            WrongQuestion wrongQuestion,
            Map<Long, Subject> subjectMap,
            Map<Long, Question> questionMap,
            Map<Long, TextbookDirectory> directoryMap
    ) {
        Subject subject = subjectMap.get(wrongQuestion.getSubjectId());
        Question question = questionMap.get(wrongQuestion.getQuestionId());
        TextbookDirectory directory = directoryMap.get(wrongQuestion.getDirectoryId());

        PracticeWrongQuestionVO wrongQuestionVO = new PracticeWrongQuestionVO();
        wrongQuestionVO.setWrongQuestionId(wrongQuestion.getId());
        wrongQuestionVO.setSubjectId(wrongQuestion.getSubjectId());
        wrongQuestionVO.setSubjectName(subject == null ? null : subject.getName());
        wrongQuestionVO.setDirectoryId(wrongQuestion.getDirectoryId());
        wrongQuestionVO.setDirectoryName(directory == null ? null : directory.getName());
        wrongQuestionVO.setQuestionId(wrongQuestion.getQuestionId());
        wrongQuestionVO.setQuestionTitle(question == null ? "题目已删除" : question.getTitle());
        wrongQuestionVO.setQuestionType(question == null ? null : question.getType());
        wrongQuestionVO.setDifficulty(question == null ? null : question.getDifficulty());
        wrongQuestionVO.setCorrectAnswer(question == null ? null : question.getAnswer());
        wrongQuestionVO.setAnalysis(question == null ? null : question.getAnalysis());
        wrongQuestionVO.setTags(wrongQuestion.getTags());
        wrongQuestionVO.setWrongCount(wrongQuestion.getWrongCount());
        wrongQuestionVO.setMasterStatus(wrongQuestion.getMasterStatus());
        wrongQuestionVO.setLastWrongTime(wrongQuestion.getLastWrongTime());
        return wrongQuestionVO;
    }

    private Map<Long, PracticeAnswerItemDTO> toAnswerMap(List<PracticeAnswerItemDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            return Map.of();
        }

        Map<Long, PracticeAnswerItemDTO> answerMap = new LinkedHashMap<>();
        for (PracticeAnswerItemDTO answer : answers) {
            if (answer == null || answer.getQuestionId() == null || answer.getQuestionId() <= 0) {
                throw new BusinessException("存在不合法的作答题目", 400);
            }
            if (answerMap.putIfAbsent(answer.getQuestionId(), answer) != null) {
                throw new BusinessException("存在重复的题目作答数据", 400);
            }
        }
        return answerMap;
    }

    private void updateUserSubjectLastPracticeTime(Long userId, Long subjectId, LocalDateTime practiceTime) {
        UserSubject userSubject = userSubjectMapper.selectOne(new LambdaQueryWrapper<UserSubject>()
                .eq(UserSubject::getUserId, userId)
                .eq(UserSubject::getSubjectId, subjectId)
                .last("LIMIT 1"));
        if (userSubject == null) {
            return;
        }
        userSubject.setLastPracticeAt(practiceTime);
        userSubjectMapper.updateById(userSubject);
    }

    private void validateSubjectFilter(Long subjectId) {
        if (subjectId == null) {
            return;
        }
        if (subjectId <= 0) {
            throw new BusinessException("学科筛选条件不合法", 400);
        }
    }

    private void validateDirectoryFilter(Long directoryId, Long subjectId) {
        if (directoryId == null) {
            return;
        }
        if (directoryId <= 0) {
            throw new BusinessException("目录筛选条件不合法", 400);
        }

        TextbookDirectory directory = textbookDirectoryMapper.selectById(directoryId);
        if (directory == null || Integer.valueOf(1).equals(directory.getDeleted())) {
            throw new BusinessException("目录不存在", 404);
        }
        if (subjectId != null && !subjectId.equals(directory.getSubjectId())) {
            throw new BusinessException("目录不属于当前学科", 400);
        }
    }

    private void validateMasterStatusFilter(Integer masterStatus) {
        if (masterStatus == null) {
            return;
        }
        if (masterStatus != 0 && masterStatus != 1) {
            throw new BusinessException("掌握状态筛选条件不合法", 400);
        }
    }

    private int normalizeMasterStatus(Integer masterStatus) {
        if (masterStatus == null || (masterStatus != 0 && masterStatus != 1)) {
            throw new BusinessException("掌握状态仅支持 0 或 1", 400);
        }
        return masterStatus;
    }

    private WrongQuestion requireWrongQuestion(Long userId, Long wrongQuestionId) {
        if (wrongQuestionId == null || wrongQuestionId <= 0) {
            throw new BusinessException("错题记录ID不合法", 400);
        }
        WrongQuestion wrongQuestion = wrongQuestionMapper.selectOne(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getId, wrongQuestionId)
                .eq(WrongQuestion::getUserId, userId)
                .last("LIMIT 1"));
        if (wrongQuestion == null) {
            throw new BusinessException("错题记录不存在或无访问权限", 404);
        }
        return wrongQuestion;
    }

    private List<PracticeWrongTrendVO> buildEmptyWrongTrends(LocalDate startDate, LocalDate endDate) {
        List<PracticeWrongTrendVO> result = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            PracticeWrongTrendVO trendVO = new PracticeWrongTrendVO();
            trendVO.setStatDate(cursor);
            trendVO.setWrongAnswerCount(0);
            trendVO.setUniqueWrongQuestionCount(0);
            result.add(trendVO);
            cursor = cursor.plusDays(1);
        }
        return result;
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
        if (tagIds.isEmpty()) {
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

    private void upsertWrongQuestion(Long userId, Question question, List<String> tagNames, LocalDateTime wrongTime) {
        WrongQuestion wrongQuestion = wrongQuestionMapper.selectOne(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getQuestionId, question.getId())
                .last("LIMIT 1"));

        String tagSnapshot = tagNames.isEmpty() ? null : String.join(", ", tagNames);
        if (wrongQuestion == null) {
            wrongQuestion = new WrongQuestion();
            wrongQuestion.setUserId(userId);
            wrongQuestion.setQuestionId(question.getId());
            wrongQuestion.setSubjectId(question.getSubjectId());
            wrongQuestion.setDirectoryId(question.getDirectoryId());
            wrongQuestion.setTags(tagSnapshot);
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setLastWrongTime(wrongTime);
            wrongQuestion.setMasterStatus(0);
            wrongQuestionMapper.insert(wrongQuestion);
            return;
        }

        wrongQuestion.setSubjectId(question.getSubjectId());
        wrongQuestion.setDirectoryId(question.getDirectoryId());
        wrongQuestion.setTags(tagSnapshot);
        wrongQuestion.setWrongCount(defaultNumber(wrongQuestion.getWrongCount()) + 1);
        wrongQuestion.setLastWrongTime(wrongTime);
        wrongQuestion.setMasterStatus(0);
        wrongQuestionMapper.updateById(wrongQuestion);
    }

    private TagSnapshot loadTagSnapshot(Long questionId) {
        List<QuestionTagRelation> relations = questionTagRelationMapper.selectList(new LambdaQueryWrapper<QuestionTagRelation>()
                .eq(QuestionTagRelation::getQuestionId, questionId));
        if (relations.isEmpty()) {
            return new TagSnapshot(List.of(), List.of());
        }

        List<Long> tagIds = relations.stream()
                .map(QuestionTagRelation::getTagId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        List<ExamTag> tags = examTagMapper.selectBatchIds(tagIds);
        Map<Long, ExamTag> tagMap = tags.stream()
                .collect(Collectors.toMap(ExamTag::getId, Function.identity(), (left, right) -> left));

        List<String> tagNames = new ArrayList<>();
        for (Long tagId : tagIds) {
            ExamTag tag = tagMap.get(tagId);
            if (tag != null && tag.getName() != null && !tag.getName().isBlank()) {
                tagNames.add(tag.getName().trim());
            }
        }
        return new TagSnapshot(tagIds, tagNames);
    }

    private int normalizePracticeMode(Integer practiceMode) {
        int mode = practiceMode == null ? PracticeModeConstants.SEQUENTIAL : practiceMode;
        if (mode != PracticeModeConstants.SEQUENTIAL
                && mode != PracticeModeConstants.RANDOM
                && mode != PracticeModeConstants.KNOWLEDGE_POINT
                && mode != PracticeModeConstants.WRONG_RETRY) {
            throw new BusinessException("当前仅支持顺序练习、随机练习、知识点练习和错题重练", 400);
        }
        return mode;
    }

    private int normalizeQuestionCount(Integer questionCount) {
        if (questionCount == null) {
            return DEFAULT_QUESTION_COUNT;
        }
        if (questionCount < 1 || questionCount > MAX_QUESTION_COUNT) {
            throw new BusinessException("题目数量范围应为 1 到 50", 400);
        }
        return questionCount;
    }

    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException("请选择知识点", 400);
        }

        List<Long> normalizedTagIds = tagIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (normalizedTagIds.isEmpty() || normalizedTagIds.size() != tagIds.size()) {
            throw new BusinessException("知识点参数不合法", 400);
        }
        return normalizedTagIds;
    }

    private int normalizeStatsDays(Integer days) {
        if (days == null) {
            return 30;
        }
        if (days != 7 && days != 30 && days != 90) {
            throw new BusinessException("统计天数仅支持 7、30、90", 400);
        }
        return days;
    }

    private int normalizeTimeCost(Integer timeCost) {
        if (timeCost == null) {
            return 0;
        }
        return Math.max(timeCost, 0);
    }

    private String normalizeSubmittedAnswer(String userAnswer) {
        if (userAnswer == null) {
            return "";
        }
        return userAnswer.trim();
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
            // Warm-up stage: avoid overestimating mastery from too few samples.
            return Math.min(resolveMasteryLevelByRate(rate, level1Rate, level2Rate, level3Rate), warmupLevelCap);
        }

        // Stable stage: classify by accuracy only.
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

    private int toInt(Long value) {
        if (value == null) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return value.intValue();
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isSessionOngoing(PracticeSession practiceSession) {
        return Integer.valueOf(PracticeModeConstants.STATUS_ONGOING).equals(practiceSession.getStatus());
    }

    private record TagSnapshot(List<Long> tagIds, List<String> tagNames) {
    }
}
