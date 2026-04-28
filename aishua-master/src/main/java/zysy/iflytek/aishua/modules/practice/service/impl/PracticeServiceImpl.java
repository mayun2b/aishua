package zysy.iflytek.aishua.modules.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zysy.iflytek.aishua.common.exception.BusinessException;
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
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PracticeServiceImpl implements PracticeService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int DEFAULT_QUESTION_COUNT = 10;
    private static final int MAX_QUESTION_COUNT = 50;

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
    private final AnswerJudgeSupport answerJudgeSupport;

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
            AnswerJudgeSupport answerJudgeSupport
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
        this.answerJudgeSupport = answerJudgeSupport;
    }

    @Override
    @Transactional
    public PracticeStartVO startPractice(Long userId, PracticeStartDTO practiceStartDTO) {
        Subject subject = requireEnabledSubject(practiceStartDTO.getSubjectId());
        requireJoinedSubject(userId, subject.getId());

        int practiceMode = normalizePracticeMode(practiceStartDTO.getPracticeMode());
        int questionCount = normalizeQuestionCount(practiceStartDTO.getQuestionCount());
        List<Question> selectedQuestions = selectSessionQuestions(subject.getId(), practiceMode, questionCount);
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
    public List<PracticeWrongQuestionVO> listWrongQuestions(Long userId, Long subjectId) {
        validateSubjectFilter(subjectId);

        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(subjectId != null, WrongQuestion::getSubjectId, subjectId)
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

        List<PracticeWrongQuestionVO> result = new ArrayList<>();
        for (WrongQuestion wrongQuestion : wrongQuestions) {
            Subject subject = subjectMap.get(wrongQuestion.getSubjectId());
            Question question = questionMap.get(wrongQuestion.getQuestionId());

            PracticeWrongQuestionVO wrongQuestionVO = new PracticeWrongQuestionVO();
            wrongQuestionVO.setWrongQuestionId(wrongQuestion.getId());
            wrongQuestionVO.setSubjectId(wrongQuestion.getSubjectId());
            wrongQuestionVO.setSubjectName(subject == null ? null : subject.getName());
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
            result.add(wrongQuestionVO);
        }
        return result;
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

    private List<Question> selectSessionQuestions(Long subjectId, int practiceMode, int questionCount) {
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
        if (mode != PracticeModeConstants.SEQUENTIAL && mode != PracticeModeConstants.RANDOM) {
            throw new BusinessException("当前最小闭环仅支持顺序练习和随机练习", 400);
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
        BigDecimal rate = defaultDecimal(correctRate);
        if (answeredCount >= 10 && rate.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return 3;
        }
        if (answeredCount >= 5 && rate.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return 2;
        }
        if (answeredCount >= 1 && rate.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return 1;
        }
        return 0;
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

    private boolean isSessionOngoing(PracticeSession practiceSession) {
        return Integer.valueOf(PracticeModeConstants.STATUS_ONGOING).equals(practiceSession.getStatus());
    }

    private record TagSnapshot(List<Long> tagIds, List<String> tagNames) {
    }
}
