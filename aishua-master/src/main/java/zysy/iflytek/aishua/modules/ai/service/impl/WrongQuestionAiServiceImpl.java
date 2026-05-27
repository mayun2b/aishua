package zysy.iflytek.aishua.modules.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.practice.entity.ExerciseRecord;
import zysy.iflytek.aishua.modules.practice.entity.UserKnowledgeMastery;
import zysy.iflytek.aishua.modules.practice.entity.WrongQuestion;
import zysy.iflytek.aishua.modules.ai.entity.WrongQuestionAiAnalysis;
import zysy.iflytek.aishua.modules.ai.entity.WrongQuestionAiChatMessage;
import zysy.iflytek.aishua.modules.ai.entity.WrongQuestionAiChatSession;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiAnalysisRequestDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiAnalysisVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatSessionVO;
import zysy.iflytek.aishua.modules.practice.mapper.ExerciseRecordMapper;
import zysy.iflytek.aishua.modules.practice.mapper.UserKnowledgeMasteryMapper;
import zysy.iflytek.aishua.modules.ai.mapper.WrongQuestionAiAnalysisMapper;
import zysy.iflytek.aishua.modules.ai.mapper.WrongQuestionAiChatMessageMapper;
import zysy.iflytek.aishua.modules.ai.mapper.WrongQuestionAiChatSessionMapper;
import zysy.iflytek.aishua.modules.practice.mapper.WrongQuestionMapper;
import zysy.iflytek.aishua.modules.ai.service.WrongQuestionAiService;
import zysy.iflytek.aishua.modules.ai.support.QwenChatClient;
import zysy.iflytek.aishua.modules.ai.support.QwenChatOptionsResolver;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.defaultNumber;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.mapRoleName;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.mapRoleToOpenAiRole;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.newCode;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.ROLE_ASSISTANT;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.ROLE_USER;

/**
 * 错题智能服务实现，负责分析、会话管理与对话持久化。 */
/**
 * 智能问答服务实现，负责相关业务逻辑与流程处理。
 */
@Slf4j
@Service
public class WrongQuestionAiServiceImpl implements WrongQuestionAiService {
    private static final int SESSION_STATUS_ONGOING = 1;
    private static final int SESSION_STATUS_CLOSED = 2;

    private static final int RECORD_STATUS_SUCCESS = 1;
    private static final int RECORD_STATUS_FAILED = 2;

    private static final int MESSAGE_TYPE_TEXT = 1;

    private static final int MAX_CONTEXT_MESSAGES = 6;
    private static final int MAX_RECENT_WRONG_RECORDS = 3;
    private static final int MAX_WEAK_KNOWLEDGE_POINTS = 8;

    private static final String PROMPT_VERSION = "wq-ai-v2-lean";

    private static final int MAX_SUMMARY_CHARS = 100;
    private static final int MAX_ERROR_REASONS = 3;
    private static final int MAX_REASON_EVIDENCE = 3;
    private static final int MAX_SOLUTION_STEPS = 3;
    private static final int MAX_KNOWLEDGE_POINTS = 3;
    private static final int MAX_AVOIDANCE_TIPS = 2;
    private static final int MAX_NORMAL_ITEM_CHARS = 28;
    private static final int MAX_SOLUTION_STEP_CHARS = 48;
    private static final int MAX_AVOIDANCE_ITEM_CHARS = 18;
    private static final int MAX_HISTORY_MESSAGE_CHARS = 120;
    private static final int MAX_CHAT_REPLY_CHARS = 120;
    private static final int MAX_CHAT_REPLY_DETAIL_CHARS = 220;
    private static final int MIN_CHAT_MAX_TOKENS = 120;
    private static final int DETAIL_CHAT_TOKEN_BOOST = 120;
    private static final int MAX_CHAT_MAX_TOKENS = 480;

    private static final String CHAT_CONCISE_POLICY_PROMPT = """
            Reply in Simplified Chinese with ultra concise style. Rules: 1) Plain text only. 2) First sentence gives conclusion. 3) Then at most 2 short reasons. 4) Default <= 120 Chinese chars. 5) If user asks for detail/examples, allow up to 220 chars. 6) If context is insufficient, state what is missing in one sentence.            """;

    private static final String ANALYSIS_SYSTEM_PROMPT = """
            You are a rigorous tutor for wrong-question analysis. Output JSON only (no markdown/code block). Required fields: summary, error_reasons, reason_evidence, solution_steps, knowledge_points, avoidance_tips. Keep concise and actionable. Use Simplified Chinese in values.            """;

    private static final String CHAT_SYSTEM_PROMPT = """
            You are a patient wrong-question coach. Answer only based on provided context and user follow-up. Give conclusion first, then short reasoning; if context is insufficient, state missing info and suggest next step.            """;

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final UserKnowledgeMasteryMapper userKnowledgeMasteryMapper;
    private final ExamTagMapper examTagMapper;
    private final WrongQuestionAiAnalysisMapper wrongQuestionAiAnalysisMapper;
    private final WrongQuestionAiChatSessionMapper wrongQuestionAiChatSessionMapper;
    private final WrongQuestionAiChatMessageMapper wrongQuestionAiChatMessageMapper;
    private final QwenChatClient qwenChatClient;
    private final QwenChatOptionsResolver qwenChatOptionsResolver;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public WrongQuestionAiServiceImpl(
            WrongQuestionMapper wrongQuestionMapper,
            QuestionMapper questionMapper,
            ExerciseRecordMapper exerciseRecordMapper,
            UserKnowledgeMasteryMapper userKnowledgeMasteryMapper,
            ExamTagMapper examTagMapper,
            WrongQuestionAiAnalysisMapper wrongQuestionAiAnalysisMapper,
            WrongQuestionAiChatSessionMapper wrongQuestionAiChatSessionMapper,
            WrongQuestionAiChatMessageMapper wrongQuestionAiChatMessageMapper,
            QwenChatClient qwenChatClient,
            QwenChatOptionsResolver qwenChatOptionsResolver
    ) {
        this.wrongQuestionMapper = wrongQuestionMapper;
        this.questionMapper = questionMapper;
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.userKnowledgeMasteryMapper = userKnowledgeMasteryMapper;
        this.examTagMapper = examTagMapper;
        this.wrongQuestionAiAnalysisMapper = wrongQuestionAiAnalysisMapper;
        this.wrongQuestionAiChatSessionMapper = wrongQuestionAiChatSessionMapper;
        this.wrongQuestionAiChatMessageMapper = wrongQuestionAiChatMessageMapper;
        this.qwenChatClient = qwenChatClient;
        this.qwenChatOptionsResolver = qwenChatOptionsResolver;
    }

    /**
     * 执行核心业务处理流程。
     */
    @Override
    public WrongQuestionAiAnalysisVO analyzeWrongQuestion(
            Long userId,
            Long wrongQuestionId,
            WrongQuestionAiAnalysisRequestDTO requestDTO
    ) {
        WrongQuestion wrongQuestion = requireWrongQuestion(userId, wrongQuestionId);
        Question question = requireQuestion(wrongQuestion.getQuestionId());

        // 构建不可变快照，便于问题复现与排查。
        JSONObject contextSnapshot = buildContextSnapshot(userId, wrongQuestion, question);
        String userPrompt = buildAnalysisUserPrompt(contextSnapshot, requestDTO == null ? null : requestDTO.getExtraInstruction());
        List<QwenChatClient.ChatMessage> messages = List.of(
                new QwenChatClient.ChatMessage("system", ANALYSIS_SYSTEM_PROMPT),
                new QwenChatClient.ChatMessage("user", userPrompt)
        );

        long beginMs = System.currentTimeMillis();
        try {
            QwenChatClient.ChatResult chatResult = qwenChatClient.chat(messages, true);
            int latencyMs = (int) (System.currentTimeMillis() - beginMs);
            JSONObject resultJson = parseModelJson(chatResult.content());

            WrongQuestionAiAnalysis analysis = new WrongQuestionAiAnalysis();
            analysis.setAnalysisCode(newCode());
            analysis.setUserId(userId);
            analysis.setWrongQuestionId(wrongQuestion.getId());
            analysis.setQuestionId(question.getId());
            analysis.setSubjectId(wrongQuestion.getSubjectId());
            analysis.setDirectoryId(wrongQuestion.getDirectoryId());
            analysis.setAnalysisType(1);
            analysis.setContextSnapshot(contextSnapshot.toJSONString());
            analysis.setResultJson(resultJson.toJSONString());
            analysis.setSummary(resultJson.getString("summary"));
            analysis.setModelProvider(qwenChatOptionsResolver.modelProvider());
            analysis.setModelName(qwenChatOptionsResolver.resolveChatModelName());
            analysis.setPromptVersion(PROMPT_VERSION);
            analysis.setPromptTokens(chatResult.usage().promptTokens());
            analysis.setCompletionTokens(chatResult.usage().completionTokens());
            analysis.setTotalTokens(chatResult.usage().totalTokens());
            analysis.setLatencyMs(latencyMs);
            analysis.setStatus(RECORD_STATUS_SUCCESS);
            analysis.setErrorMessage(null);
            wrongQuestionAiAnalysisMapper.insert(analysis);
            return toAnalysisVO(analysis, resultJson);
        } catch (BusinessException exception) {
            persistFailedAnalysis(userId, wrongQuestion, question, contextSnapshot, exception.getMessage(),
                    (int) (System.currentTimeMillis() - beginMs));
            throw exception;
        } catch (Exception exception) {
            persistFailedAnalysis(userId, wrongQuestion, question, contextSnapshot, "智能分析失败",
                    (int) (System.currentTimeMillis() - beginMs));
            log.error("Wrong-question 智能分析失败, userId={}, wrongQuestionId={}", userId, wrongQuestionId, exception);
            throw new BusinessException("智能分析失败，请稍后重试", 500);
        }
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public WrongQuestionAiAnalysisVO getLatestAnalysis(Long userId, Long wrongQuestionId) {
        requireWrongQuestion(userId, wrongQuestionId);
        WrongQuestionAiAnalysis analysis = wrongQuestionAiAnalysisMapper.selectOne(new LambdaQueryWrapper<WrongQuestionAiAnalysis>()
                .eq(WrongQuestionAiAnalysis::getUserId, userId)
                .eq(WrongQuestionAiAnalysis::getWrongQuestionId, wrongQuestionId)
                .orderByDesc(WrongQuestionAiAnalysis::getCreateTime)
                .orderByDesc(WrongQuestionAiAnalysis::getId)
                .last("LIMIT 1"));
        if (analysis == null) {
            throw new BusinessException("当前错题不存在智能分析记录", 404);
        }
        return toAnalysisVO(analysis, parseStoredJson(analysis.getResultJson()));
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public WrongQuestionAiChatSessionVO getLatestChatSession(Long userId, Long wrongQuestionId) {
        requireWrongQuestion(userId, wrongQuestionId);
        WrongQuestionAiChatSession session = findLatestAssistantSession(userId, wrongQuestionId);
        if (session == null) {
            throw new BusinessException("\u5f53\u524d\u9519\u9898\u6682\u65e0\u52a9\u624b\u4f1a\u8bdd", 404);
        }
        return toSessionVO(session);
    }

    /**
     * 执行创建业务流程并返回结果。
     */
    @Override
    @Transactional
    public WrongQuestionAiChatSessionVO createChatSession(
            Long userId,
            Long wrongQuestionId,
            WrongQuestionAiCreateSessionDTO requestDTO
    ) {
        WrongQuestion wrongQuestion = requireWrongQuestion(userId, wrongQuestionId);

        Long analysisId = requestDTO == null ? null : requestDTO.getAnalysisId();
        if (analysisId != null) {
            requireAnalysis(userId, wrongQuestionId, analysisId);
        } else {
            WrongQuestionAiAnalysis latest = wrongQuestionAiAnalysisMapper.selectOne(new LambdaQueryWrapper<WrongQuestionAiAnalysis>()
                    .eq(WrongQuestionAiAnalysis::getUserId, userId)
                    .eq(WrongQuestionAiAnalysis::getWrongQuestionId, wrongQuestionId)
                    .eq(WrongQuestionAiAnalysis::getStatus, RECORD_STATUS_SUCCESS)
                    .orderByDesc(WrongQuestionAiAnalysis::getCreateTime)
                    .orderByDesc(WrongQuestionAiAnalysis::getId)
                    .last("LIMIT 1"));
            analysisId = latest == null ? null : latest.getId();
        }

        WrongQuestionAiChatSession existing = findLatestAssistantSession(userId, wrongQuestionId);
        if (existing != null) {
            boolean shouldUpdate = false;
            if (!isSessionOngoing(existing.getStatus())) {
                existing.setStatus(SESSION_STATUS_ONGOING);
                shouldUpdate = true;
            }
            if (analysisId != null && !analysisId.equals(existing.getAnalysisId())) {
                existing.setAnalysisId(analysisId);
                shouldUpdate = true;
            }
            if (shouldUpdate) {
                existing.setLastMessageAt(LocalDateTime.now());
                wrongQuestionAiChatSessionMapper.updateById(existing);
            }
            return toSessionVO(existing);
        }

        WrongQuestionAiChatSession session = new WrongQuestionAiChatSession();
        session.setSessionCode(newCode());
        session.setUserId(userId);
        session.setWrongQuestionId(wrongQuestionId);
        session.setAnalysisId(analysisId);
        session.setQuestionId(wrongQuestion.getQuestionId());
        session.setSubjectId(wrongQuestion.getSubjectId());
        session.setStatus(SESSION_STATUS_ONGOING);
        session.setRoundCount(0);
        session.setLastMessageAt(LocalDateTime.now());
        session.setTotalPromptTokens(0);
        session.setTotalCompletionTokens(0);
        session.setTotalTokens(0);
        wrongQuestionAiChatSessionMapper.insert(session);
        return toSessionVO(session);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<WrongQuestionAiChatMessageVO> listChatMessages(Long userId, Long wrongQuestionId, Long sessionId) {
        requireWrongQuestion(userId, wrongQuestionId);
        List<WrongQuestionAiChatMessage> messages = wrongQuestionAiChatMessageMapper.selectList(
                new LambdaQueryWrapper<WrongQuestionAiChatMessage>()
                        .inSql(
                                WrongQuestionAiChatMessage::getSessionId,
                                "SELECT id FROM wrong_question_ai_chat_session WHERE user_id = " + userId
                                        + " AND wrong_question_id = " + wrongQuestionId + " AND deleted = 0"
                        )
                        .orderByAsc(WrongQuestionAiChatMessage::getCreateTime)
                        .orderByAsc(WrongQuestionAiChatMessage::getId)
        );
        return messages.stream().map(this::toMessageVO).toList();
    }

    /**
     * 执行核心业务处理流程。
     */
    @Override
    public WrongQuestionAiChatMessageVO sendChatMessage(
            Long userId,
            Long wrongQuestionId,
            Long sessionId,
            WrongQuestionAiSendMessageDTO requestDTO
    ) {
        return doSendChatMessage(userId, wrongQuestionId, sessionId, requestDTO, null);
    }

    /**
     * 执行核心业务处理流程。
     */
    @Override
    public WrongQuestionAiChatMessageVO streamChatMessage(
            Long userId,
            Long wrongQuestionId,
            Long sessionId,
            WrongQuestionAiSendMessageDTO requestDTO,
            Consumer<String> chunkConsumer
    ) {
        return doSendChatMessage(userId, wrongQuestionId, sessionId, requestDTO, chunkConsumer);
    }

    /**
     * 执行核心业务处理流程。
     */
    private WrongQuestionAiChatMessageVO doSendChatMessage(
            Long userId,
            Long wrongQuestionId,
            Long sessionId,
            WrongQuestionAiSendMessageDTO requestDTO,
            Consumer<String> chunkConsumer
    ) {
        WrongQuestionAiChatSession session = requireSession(userId, wrongQuestionId, sessionId);
        if (!isSessionOngoing(session.getStatus())) {
            throw new BusinessException("当前对话会话已关闭，请新建会话后重试", 400);
        }

        WrongQuestion wrongQuestion = requireWrongQuestion(userId, wrongQuestionId);
        Question question = requireQuestion(wrongQuestion.getQuestionId());
        String content = normalizeInputContent(requestDTO.getContent());

        int nextSeq = nextSeqNo(sessionId);
        WrongQuestionAiChatMessage userMessage = new WrongQuestionAiChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setSeqNo(nextSeq);
        userMessage.setRole(ROLE_USER);
        userMessage.setMessageType(MESSAGE_TYPE_TEXT);
        userMessage.setContentText(content);
        userMessage.setStatus(RECORD_STATUS_SUCCESS);
        userMessage.setPromptTokens(0);
        userMessage.setCompletionTokens(0);
        userMessage.setTotalTokens(0);
        userMessage.setLatencyMs(0);
        wrongQuestionAiChatMessageMapper.insert(userMessage);

        // 组合题目事实、最新分析和近期对话，构建上下文。
        JSONObject chatContext = buildChatContext(userId, wrongQuestion, question, session);
        List<QwenChatClient.ChatMessage> messages = buildChatPromptMessages(chatContext, userId, wrongQuestionId, content);
        int chatMaxTokens = resolveChatMaxTokens(content);
        int maxReplyChars = qwenChatOptionsResolver.shouldAllowDetailedAnswer(content)
                ? MAX_CHAT_REPLY_DETAIL_CHARS
                : MAX_CHAT_REPLY_CHARS;

        long beginMs = System.currentTimeMillis();
        try {
            QwenChatClient.ChatResult chatResult = chunkConsumer == null
                    ? qwenChatClient.chat(messages, false, chatMaxTokens)
                    : qwenChatClient.chatStream(messages, false, chatMaxTokens, chunkConsumer);
            int latencyMs = (int) (System.currentTimeMillis() - beginMs);
            String assistantContent = compactChatReply(chatResult.content(), maxReplyChars);

            WrongQuestionAiChatMessage assistantMessage = new WrongQuestionAiChatMessage();
            assistantMessage.setSessionId(sessionId);
            assistantMessage.setSeqNo(nextSeq + 1);
            assistantMessage.setRole(ROLE_ASSISTANT);
            assistantMessage.setMessageType(MESSAGE_TYPE_TEXT);
            assistantMessage.setContentText(assistantContent);
            assistantMessage.setModelProvider(qwenChatOptionsResolver.modelProvider());
            assistantMessage.setModelName(qwenChatOptionsResolver.resolveChatModelName());
            assistantMessage.setPromptTokens(chatResult.usage().promptTokens());
            assistantMessage.setCompletionTokens(chatResult.usage().completionTokens());
            assistantMessage.setTotalTokens(chatResult.usage().totalTokens());
            assistantMessage.setLatencyMs(latencyMs);
            assistantMessage.setStatus(RECORD_STATUS_SUCCESS);
            wrongQuestionAiChatMessageMapper.insert(assistantMessage);

            updateSessionAfterReply(session, chatResult.usage(), LocalDateTime.now(), true);
            return toMessageVO(assistantMessage);
        } catch (BusinessException exception) {
            WrongQuestionAiChatMessage failedMessage = new WrongQuestionAiChatMessage();
            failedMessage.setSessionId(sessionId);
            failedMessage.setSeqNo(nextSeq + 1);
            failedMessage.setRole(ROLE_ASSISTANT);
            failedMessage.setMessageType(MESSAGE_TYPE_TEXT);
            failedMessage.setContentText(null);
            failedMessage.setModelProvider(qwenChatOptionsResolver.modelProvider());
            failedMessage.setModelName(qwenChatOptionsResolver.resolveChatModelName());
            failedMessage.setStatus(RECORD_STATUS_FAILED);
            failedMessage.setErrorMessage(exception.getMessage());
            failedMessage.setLatencyMs((int) (System.currentTimeMillis() - beginMs));
            wrongQuestionAiChatMessageMapper.insert(failedMessage);

            updateSessionAfterReply(session, new QwenChatClient.Usage(0, 0, 0), LocalDateTime.now(), false);
            throw exception;
        }
    }
    /**
     * 执行删除与清理业务流程。
     */
    @Override
    @Transactional
    public WrongQuestionAiChatSessionVO closeChatSession(Long userId, Long wrongQuestionId, Long sessionId) {
        WrongQuestionAiChatSession session = requireSession(userId, wrongQuestionId, sessionId);
        if (isSessionClosed(session.getStatus())) {
            return toSessionVO(session);
        }
        if (!hasUserMessageContent(sessionId)) {
            wrongQuestionAiChatSessionMapper.deleteById(sessionId);
            session.setStatus(SESSION_STATUS_CLOSED);
            session.setLastMessageAt(LocalDateTime.now());
            return toSessionVO(session);
        }
        session.setStatus(SESSION_STATUS_CLOSED);
        session.setLastMessageAt(LocalDateTime.now());
        wrongQuestionAiChatSessionMapper.updateById(session);
        return toSessionVO(session);
    }

    /**
     * 执行保存与更新业务流程。
     */
    private void persistFailedAnalysis(
            Long userId,
            WrongQuestion wrongQuestion,
            Question question,
            JSONObject contextSnapshot,
            String errorMessage,
            int latencyMs
    ) {
        WrongQuestionAiAnalysis failed = new WrongQuestionAiAnalysis();
        failed.setAnalysisCode(newCode());
        failed.setUserId(userId);
        failed.setWrongQuestionId(wrongQuestion.getId());
        failed.setQuestionId(question.getId());
        failed.setSubjectId(wrongQuestion.getSubjectId());
        failed.setDirectoryId(wrongQuestion.getDirectoryId());
        failed.setAnalysisType(1);
        failed.setContextSnapshot(contextSnapshot.toJSONString());
        failed.setResultJson(null);
        failed.setSummary(null);
        failed.setModelProvider(qwenChatOptionsResolver.modelProvider());
        failed.setModelName(qwenChatOptionsResolver.resolveChatModelName());
        failed.setPromptVersion(PROMPT_VERSION);
        failed.setPromptTokens(0);
        failed.setCompletionTokens(0);
        failed.setTotalTokens(0);
        failed.setLatencyMs(Math.max(latencyMs, 0));
        failed.setStatus(RECORD_STATUS_FAILED);
        failed.setErrorMessage(errorMessage);
        wrongQuestionAiAnalysisMapper.insert(failed);
    }

    /**
     * 构建业务处理所需数据。
     */
    private JSONObject buildContextSnapshot(Long userId, WrongQuestion wrongQuestion, Question question) {
        JSONObject context = new JSONObject();
        context.put("userId", userId);
        context.put("wrongQuestionId", wrongQuestion.getId());

        JSONObject questionNode = new JSONObject();
        questionNode.put("questionId", question.getId());
        questionNode.put("title", question.getTitle());
        questionNode.put("content", question.getContent());
        questionNode.put("type", question.getType());
        questionNode.put("options", parseJsonIfPossible(question.getOptions()));
        questionNode.put("answer", question.getAnswer());
        questionNode.put("analysis", question.getAnalysis());
        questionNode.put("difficulty", question.getDifficulty());
        questionNode.put("imageUrls", question.getImageUrls());
        questionNode.put("imageDesc", question.getImageDesc());
        context.put("question", questionNode);

        JSONObject wrongNode = new JSONObject();
        wrongNode.put("wrongCount", wrongQuestion.getWrongCount());
        wrongNode.put("masterStatus", wrongQuestion.getMasterStatus());
        wrongNode.put("lastWrongTime", wrongQuestion.getLastWrongTime());
        wrongNode.put("tags", wrongQuestion.getTags());
        context.put("wrongQuestion", wrongNode);

        context.put("recentWrongRecords", loadRecentWrongRecords(userId, question.getId()));
        // 注入薄弱知识画像，支持个性化诊断。
        context.put("knowledgeProfile", loadKnowledgeProfile(userId, question.getSubjectId()));
        context.put("generatedAt", LocalDateTime.now().toString());
        return context;
    }

    /**
     * 执行核心业务处理流程。
     */
    private JSONArray loadRecentWrongRecords(Long userId, Long questionId) {
        List<ExerciseRecord> records = exerciseRecordMapper.selectList(new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, userId)
                .eq(ExerciseRecord::getQuestionId, questionId)
                .eq(ExerciseRecord::getIsCorrect, 0)
                .orderByDesc(ExerciseRecord::getExerciseTime)
                .orderByDesc(ExerciseRecord::getId)
                .last("LIMIT " + MAX_RECENT_WRONG_RECORDS));

        JSONArray array = new JSONArray();
        for (ExerciseRecord record : records) {
            JSONObject item = new JSONObject();
            item.put("recordId", record.getId());
            item.put("userAnswer", record.getUserAnswer());
            item.put("timeCost", record.getTimeCost());
            item.put("exerciseTime", record.getExerciseTime());
            array.add(item);
        }
        return array;
    }

    /**
     * 执行核心业务处理流程。
     */
    private JSONArray loadKnowledgeProfile(Long userId, Long subjectId) {
        if (subjectId == null || subjectId <= 0) {
            return new JSONArray();
        }
        List<UserKnowledgeMastery> masteries = userKnowledgeMasteryMapper.selectList(
                new LambdaQueryWrapper<UserKnowledgeMastery>()
                        .eq(UserKnowledgeMastery::getUserId, userId)
                        .eq(UserKnowledgeMastery::getSubjectId, subjectId)
                        .orderByAsc(UserKnowledgeMastery::getMasteryLevel)
                        .orderByDesc(UserKnowledgeMastery::getWrongCount)
                        .orderByDesc(UserKnowledgeMastery::getTotalCount)
                        .last("LIMIT " + MAX_WEAK_KNOWLEDGE_POINTS)
        );
        if (masteries.isEmpty()) {
            return new JSONArray();
        }

        Map<Long, String> tagNameMap = loadTagNameMap(masteries.stream()
                .map(UserKnowledgeMastery::getTagId)
                .filter(tagId -> tagId != null && tagId > 0)
                .distinct()
                .toList());
        JSONArray array = new JSONArray();
        for (UserKnowledgeMastery mastery : masteries) {
            JSONObject item = new JSONObject();
            item.put("tagId", mastery.getTagId());
            item.put("tagName", tagNameMap.getOrDefault(mastery.getTagId(), null));
            item.put("masteryLevel", mastery.getMasteryLevel());
            item.put("wrongCount", mastery.getWrongCount());
            item.put("totalCount", mastery.getTotalCount());
            item.put("correctRate", mastery.getCorrectRate());
            array.add(item);
        }
        return array;
    }

    /**
     * 执行核心业务处理流程。
     */
    private Map<Long, String> loadTagNameMap(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ExamTag> tags = examTagMapper.selectBatchIds(tagIds);
        Map<Long, String> map = new HashMap<>();
        for (ExamTag tag : tags) {
            if (tag != null) {
                map.put(tag.getId(), tag.getName());
            }
        }
        return map;
    }

    /**
     * 构建业务处理所需数据。
     */
    private String buildAnalysisUserPrompt(JSONObject contextSnapshot, String extraInstruction) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("\u8bf7\u5bf9\u4ee5\u4e0b\u9519\u9898\u8fdb\u884c\u5206\u6790\uff0c\u5e76\u8fd4\u56de\u0020\u004a\u0053\u004f\u004e\u0020\u5bf9\u8c61\u3002\\n");
        if (StringUtils.hasText(extraInstruction)) {
            prompt.append("\u7528\u6237\u8865\u5145\u8981\u6c42\uff1a").append(extraInstruction.trim()).append("\\n");
        }
        prompt.append("\u9519\u9898\u4e0a\u4e0b\u6587\uff1a\\n").append(contextSnapshot.toJSONString());
        return prompt.toString();
    }

    /**
     * 解析并转换输入数据。
     */
    private JSONObject parseModelJson(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("智能服务响应内容为空", 502);
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replace("```json", "")
                    .replace("```JSON", "")
                    .replace("```", "")
                    .trim();
        }
        try {
            return JSON.parseObject(trimmed);
        } catch (Exception ignored) {
            int first = trimmed.indexOf('{');
            int last = trimmed.lastIndexOf('}');
            if (first >= 0 && last > first) {
                try {
                    return JSON.parseObject(trimmed.substring(first, last + 1));
                } catch (Exception ignoredAgain) {
                    throw new BusinessException("智能服务响应格式不合法", 502);
                }
            }
            throw new BusinessException("智能服务响应格式不合法", 502);
        }
    }

    /**
     * 解析并转换输入数据。
     */
    private JSONObject parseStoredJson(String value) {
        if (!StringUtils.hasText(value)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(value);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    /**
     * 解析并转换输入数据。
     */
    private Object parseJsonIfPossible(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return JSON.parse(value);
        } catch (Exception ignored) {
            return value;
        }
    }

    /**
     * 构建业务处理所需数据。
     */
    private List<QwenChatClient.ChatMessage> buildChatPromptMessages(
            JSONObject context,
            Long userId,
            Long wrongQuestionId,
            String currentUserQuestion
    ) {
        List<QwenChatClient.ChatMessage> messages = new ArrayList<>();
        messages.add(new QwenChatClient.ChatMessage("system", CHAT_SYSTEM_PROMPT));
        messages.add(new QwenChatClient.ChatMessage("system", CHAT_CONCISE_POLICY_PROMPT));
        messages.add(new QwenChatClient.ChatMessage("system", "Question context: " + context.toJSONString()));

        // 跨会话回放同一错题的近期有效消息。
        List<WrongQuestionAiChatMessage> historyMessages = wrongQuestionAiChatMessageMapper.selectList(
                new LambdaQueryWrapper<WrongQuestionAiChatMessage>()
                        .inSql(
                                WrongQuestionAiChatMessage::getSessionId,
                                "SELECT id FROM wrong_question_ai_chat_session WHERE user_id = " + userId
                                        + " AND wrong_question_id = " + wrongQuestionId + " AND deleted = 0"
                        )
                        .eq(WrongQuestionAiChatMessage::getStatus, RECORD_STATUS_SUCCESS)
                        .orderByDesc(WrongQuestionAiChatMessage::getCreateTime)
                        .orderByDesc(WrongQuestionAiChatMessage::getId)
                        .last("LIMIT " + MAX_CONTEXT_MESSAGES)
        );
        historyMessages.sort(
                Comparator.comparing(WrongQuestionAiChatMessage::getCreateTime)
                        .thenComparing(WrongQuestionAiChatMessage::getId)
        );
        for (WrongQuestionAiChatMessage history : historyMessages) {
            String role = mapRoleToOpenAiRole(history.getRole());
            if (!StringUtils.hasText(role) || !StringUtils.hasText(history.getContentText())) {
                continue;
            }
            messages.add(new QwenChatClient.ChatMessage(role, compactText(history.getContentText(), MAX_HISTORY_MESSAGE_CHARS)));
        }

        messages.add(new QwenChatClient.ChatMessage("user", currentUserQuestion));
        return messages;
    }

    /**
     * 构建业务处理所需数据。
     */
    private JSONObject buildChatContext(
            Long userId,
            WrongQuestion wrongQuestion,
            Question question,
            WrongQuestionAiChatSession session
    ) {
        JSONObject context = new JSONObject();
        context.put("userId", userId);
        context.put("wrongQuestionId", wrongQuestion.getId());
        context.put("questionId", question.getId());
        context.put("questionTitle", compactText(question.getTitle(), 80));
        context.put("questionType", question.getType());
        context.put("difficulty", question.getDifficulty());
        context.put("standardAnswer", compactText(question.getAnswer(), 120));
        context.put("officialAnalysis", compactText(question.getAnalysis(), 160));
        context.put("sessionCode", session.getSessionCode());

        WrongQuestionAiAnalysis analysis = null;
        if (session.getAnalysisId() != null && session.getAnalysisId() > 0) {
            analysis = wrongQuestionAiAnalysisMapper.selectById(session.getAnalysisId());
        }
        if (analysis == null) {
            analysis = wrongQuestionAiAnalysisMapper.selectOne(new LambdaQueryWrapper<WrongQuestionAiAnalysis>()
                    .eq(WrongQuestionAiAnalysis::getUserId, userId)
                    .eq(WrongQuestionAiAnalysis::getWrongQuestionId, wrongQuestion.getId())
                    .eq(WrongQuestionAiAnalysis::getStatus, RECORD_STATUS_SUCCESS)
                    .orderByDesc(WrongQuestionAiAnalysis::getCreateTime)
                    .orderByDesc(WrongQuestionAiAnalysis::getId)
                    .last("LIMIT 1"));
        }
        if (analysis != null) {
            context.put("latestAnalysisSummary", compactText(analysis.getSummary(), 120));
        }
        return context;
    }

    /**
     * 执行保存与更新业务流程。
     */
    private void updateSessionAfterReply(
            WrongQuestionAiChatSession session,
            QwenChatClient.Usage usage,
            LocalDateTime now,
            boolean assistantSuccess
    ) {
        session.setLastMessageAt(now);
        session.setTotalPromptTokens(defaultNumber(session.getTotalPromptTokens()) + usage.promptTokens());
        session.setTotalCompletionTokens(defaultNumber(session.getTotalCompletionTokens()) + usage.completionTokens());
        session.setTotalTokens(defaultNumber(session.getTotalTokens()) + usage.totalTokens());
        if (assistantSuccess) {
            session.setRoundCount(defaultNumber(session.getRoundCount()) + 1);
        }
        wrongQuestionAiChatSessionMapper.updateById(session);
    }

    /**
     * 执行参数与状态校验。
     */
    private WrongQuestion requireWrongQuestion(Long userId, Long wrongQuestionId) {
        WrongQuestion wrongQuestion = wrongQuestionMapper.selectOne(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getId, wrongQuestionId)
                .eq(WrongQuestion::getUserId, userId)
                .last("LIMIT 1"));
        if (wrongQuestion == null) {
            throw new BusinessException("错题记录不存在或无访问权限", 404);
        }
        return wrongQuestion;
    }

    /**
     * 执行参数与状态校验。
     */
    private Question requireQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || Integer.valueOf(1).equals(question.getDeleted())) {
            throw new BusinessException("题目不存在或已被删除", 404);
        }
        return question;
    }

    /**
     * 执行参数与状态校验。
     */
    private WrongQuestionAiAnalysis requireAnalysis(Long userId, Long wrongQuestionId, Long analysisId) {
        WrongQuestionAiAnalysis analysis = wrongQuestionAiAnalysisMapper.selectOne(new LambdaQueryWrapper<WrongQuestionAiAnalysis>()
                .eq(WrongQuestionAiAnalysis::getId, analysisId)
                .eq(WrongQuestionAiAnalysis::getUserId, userId)
                .eq(WrongQuestionAiAnalysis::getWrongQuestionId, wrongQuestionId)
                .last("LIMIT 1"));
        if (analysis == null) {
            throw new BusinessException("分析记录不存在或无访问权限", 404);
        }
        return analysis;
    }

    /**
     * 执行参数与状态校验。
     */
    private WrongQuestionAiChatSession requireSession(Long userId, Long wrongQuestionId, Long sessionId) {
        WrongQuestionAiChatSession session = wrongQuestionAiChatSessionMapper.selectOne(new LambdaQueryWrapper<WrongQuestionAiChatSession>()
                .eq(WrongQuestionAiChatSession::getId, sessionId)
                .eq(WrongQuestionAiChatSession::getUserId, userId)
                .eq(WrongQuestionAiChatSession::getWrongQuestionId, wrongQuestionId)
                .last("LIMIT 1"));
        if (session == null) {
            throw new BusinessException("会话不存在或无访问权限", 404);
        }
        return session;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    private WrongQuestionAiChatSession findLatestAssistantSession(Long userId, Long wrongQuestionId) {
        return wrongQuestionAiChatSessionMapper.selectOne(
                new LambdaQueryWrapper<WrongQuestionAiChatSession>()
                        .eq(WrongQuestionAiChatSession::getUserId, userId)
                        .eq(WrongQuestionAiChatSession::getWrongQuestionId, wrongQuestionId)
                        .orderByDesc(WrongQuestionAiChatSession::getLastMessageAt)
                        .orderByDesc(WrongQuestionAiChatSession::getId)
                        .last("LIMIT 1")
        );
    }

    /**
     * 计算并返回处理结果。
     */
    private int nextSeqNo(Long sessionId) {
        WrongQuestionAiChatMessage latest = wrongQuestionAiChatMessageMapper.selectOne(
                new LambdaQueryWrapper<WrongQuestionAiChatMessage>()
                        .eq(WrongQuestionAiChatMessage::getSessionId, sessionId)
                        .orderByDesc(WrongQuestionAiChatMessage::getSeqNo)
                        .orderByDesc(WrongQuestionAiChatMessage::getId)
                        .last("LIMIT 1")
        );
        return latest == null ? 1 : defaultNumber(latest.getSeqNo()) + 1;
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean hasUserMessageContent(Long sessionId) {
        Number count = wrongQuestionAiChatMessageMapper.selectCount(
                new LambdaQueryWrapper<WrongQuestionAiChatMessage>()
                        .eq(WrongQuestionAiChatMessage::getSessionId, sessionId)
                        .eq(WrongQuestionAiChatMessage::getRole, ROLE_USER)
        );
        return count != null && count.longValue() > 0;
    }

    /**
     * 执行核心业务处理流程。
     */
    private WrongQuestionAiAnalysisVO toAnalysisVO(WrongQuestionAiAnalysis analysis, JSONObject resultJson) {
        WrongQuestionAiAnalysisVO vo = new WrongQuestionAiAnalysisVO();
        vo.setAnalysisId(analysis.getId());
        vo.setAnalysisCode(analysis.getAnalysisCode());
        vo.setWrongQuestionId(analysis.getWrongQuestionId());
        vo.setQuestionId(analysis.getQuestionId());
        vo.setSummary(compactText(analysis.getSummary(), MAX_SUMMARY_CHARS));
        vo.setStatus(analysis.getStatus());
        vo.setErrorMessage(analysis.getErrorMessage());
        vo.setPromptTokens(defaultNumber(analysis.getPromptTokens()));
        vo.setCompletionTokens(defaultNumber(analysis.getCompletionTokens()));
        vo.setTotalTokens(defaultNumber(analysis.getTotalTokens()));
        vo.setLatencyMs(defaultNumber(analysis.getLatencyMs()));
        vo.setCreateTime(analysis.getCreateTime());

        vo.setErrorReasons(compactList(resultJson.getJSONArray("error_reasons"), MAX_ERROR_REASONS, MAX_NORMAL_ITEM_CHARS));
        vo.setReasonEvidence(compactList(resultJson.getJSONArray("reason_evidence"), MAX_REASON_EVIDENCE, MAX_NORMAL_ITEM_CHARS));
        vo.setSolutionSteps(compactList(resultJson.getJSONArray("solution_steps"), MAX_SOLUTION_STEPS, MAX_SOLUTION_STEP_CHARS));
        vo.setKnowledgePoints(compactList(resultJson.getJSONArray("knowledge_points"), MAX_KNOWLEDGE_POINTS, MAX_NORMAL_ITEM_CHARS));
        vo.setAvoidanceTips(compactList(resultJson.getJSONArray("avoidance_tips"), MAX_AVOIDANCE_TIPS, MAX_AVOIDANCE_ITEM_CHARS));
        // 精简分析模式不生成追问题目。
        vo.setFollowUpQuestions(Collections.emptyList());
        if (!StringUtils.hasText(vo.getSummary())) {
            vo.setSummary(compactText(resultJson.getString("summary"), MAX_SUMMARY_CHARS));
        }
        return vo;
    }

    /**
     * 执行核心业务处理流程。
     */
    private List<String> toStringList(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            Object item = array.get(i);
            if (item == null) {
                continue;
            }
            String value = String.valueOf(item).trim();
            if (!value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * 执行核心业务处理流程。
     */
    private List<String> compactList(JSONArray array, int maxItems, int maxChars) {
        List<String> source = toStringList(array);
        if (source.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> compacted = new ArrayList<>();
        for (String item : source) {
            if (compacted.size() >= maxItems) {
                break;
            }
            String value = compactText(item, maxChars);
            if (StringUtils.hasText(value)) {
                compacted.add(value);
            }
        }
        return compacted;
    }

    /**
     * 执行核心业务处理流程。
     */
    private String compactText(String text, int maxChars) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars);
    }

    /**
     * 执行核心业务处理流程。
     */
    private WrongQuestionAiChatSessionVO toSessionVO(WrongQuestionAiChatSession session) {
        WrongQuestionAiChatSessionVO vo = new WrongQuestionAiChatSessionVO();
        vo.setSessionId(session.getId());
        vo.setSessionCode(session.getSessionCode());
        vo.setWrongQuestionId(session.getWrongQuestionId());
        vo.setAnalysisId(session.getAnalysisId());
        vo.setStatus(session.getStatus());
        vo.setRoundCount(defaultNumber(session.getRoundCount()));
        vo.setLastMessageAt(session.getLastMessageAt());
        vo.setTotalPromptTokens(defaultNumber(session.getTotalPromptTokens()));
        vo.setTotalCompletionTokens(defaultNumber(session.getTotalCompletionTokens()));
        vo.setTotalTokens(defaultNumber(session.getTotalTokens()));
        vo.setCreateTime(session.getCreateTime());
        return vo;
    }

    /**
     * 执行核心业务处理流程。
     */
    private WrongQuestionAiChatMessageVO toMessageVO(WrongQuestionAiChatMessage message) {
        WrongQuestionAiChatMessageVO vo = new WrongQuestionAiChatMessageVO();
        vo.setMessageId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSeqNo(message.getSeqNo());
        vo.setRole(message.getRole());
        vo.setRoleName(mapRoleName(message.getRole()));
        vo.setContent(message.getContentText());
        vo.setStatus(message.getStatus());
        vo.setErrorMessage(message.getErrorMessage());
        vo.setPromptTokens(defaultNumber(message.getPromptTokens()));
        vo.setCompletionTokens(defaultNumber(message.getCompletionTokens()));
        vo.setTotalTokens(defaultNumber(message.getTotalTokens()));
        vo.setLatencyMs(defaultNumber(message.getLatencyMs()));
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    /**
     * 解析并转换输入数据。
     */
    private String normalizeInputContent(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("题目内容不能为空", 400);
        }
        return normalized;
    }

    /**
     * 解析并转换输入数据。
     */
    private int resolveChatMaxTokens(String currentUserQuestion) {
        int safeBase = qwenChatOptionsResolver.resolveBoundedChatMaxTokens(
                MIN_CHAT_MAX_TOKENS,
                Integer.MAX_VALUE
        );
        if (qwenChatOptionsResolver.shouldAllowDetailedAnswer(currentUserQuestion)) {
            return Math.min(safeBase + DETAIL_CHAT_TOKEN_BOOST, MAX_CHAT_MAX_TOKENS);
        }
        return safeBase;
    }

    /**
     * 执行核心业务处理流程。
     */
    private String compactChatReply(String text, int maxChars) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .replaceAll("(?m)^[-*]\\s+", "")
                .replaceAll("(?m)^\\d+\\.\\s+", "")
                .trim()
                .replaceAll("\\s+", " ");
        if (!StringUtils.hasText(normalized)) {
            normalized = text.trim().replaceAll("\\s+", " ");
        }
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars);
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean isSessionOngoing(Integer status) {
        return status != null && status == SESSION_STATUS_ONGOING;
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean isSessionClosed(Integer status) {
        return status != null && status == SESSION_STATUS_CLOSED;
    }
}
