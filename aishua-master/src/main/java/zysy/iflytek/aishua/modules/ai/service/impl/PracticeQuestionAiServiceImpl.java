package zysy.iflytek.aishua.modules.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.modules.ai.entity.PracticeQuestionAiChatMessage;
import zysy.iflytek.aishua.modules.ai.entity.PracticeQuestionAiChatSession;
import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatSessionVO;
import zysy.iflytek.aishua.modules.ai.mapper.PracticeQuestionAiChatMessageMapper;
import zysy.iflytek.aishua.modules.ai.mapper.PracticeQuestionAiChatSessionMapper;
import zysy.iflytek.aishua.modules.ai.service.PracticeQuestionAiService;
import zysy.iflytek.aishua.modules.ai.support.QwenChatClient;
import zysy.iflytek.aishua.modules.ai.support.QwenChatOptionsResolver;
import zysy.iflytek.aishua.modules.practice.entity.ExerciseRecord;
import zysy.iflytek.aishua.modules.practice.entity.PracticeSession;
import zysy.iflytek.aishua.modules.practice.mapper.ExerciseRecordMapper;
import zysy.iflytek.aishua.modules.practice.mapper.PracticeSessionMapper;
import zysy.iflytek.aishua.modules.practice.support.PracticeModeConstants;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.defaultNumber;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.mapRoleName;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.mapRoleToOpenAiRole;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.newCode;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.ROLE_ASSISTANT;
import static zysy.iflytek.aishua.modules.ai.support.AiServiceSupport.ROLE_USER;

/**
 * 练习题智能助教服务实现，负责引导对话、流式回复与会话统计。 */
/**
 * 智能问答服务实现，负责相关业务逻辑与流程处理。
 */
@Slf4j
@Service
public class PracticeQuestionAiServiceImpl implements PracticeQuestionAiService {
    private static final int SESSION_STATUS_ONGOING = 1;
    private static final int SESSION_STATUS_CLOSED = 2;

    private static final int SESSION_TRIGGER_SOURCE_MANUAL = 1;
    private static final int SESSION_TRIGGER_SOURCE_AUTO = 2;

    private static final int RECORD_STATUS_SUCCESS = 1;
    private static final int RECORD_STATUS_FAILED = 2;

    private static final int MESSAGE_TYPE_TEXT = 1;

    private static final int MAX_CONTEXT_MESSAGES = 8;
    private static final int MAX_HISTORY_MESSAGE_CHARS = 180;
    private static final int MAX_QUESTION_CONTENT_CHARS = 360;
    private static final int MAX_INTERNAL_ANSWER_CHARS = 280;
    private static final int MAX_INTERNAL_ANALYSIS_CHARS = 320;
    private static final int MAX_DRAFT_ANSWER_CHARS = 360;
    private static final int MAX_CHAT_REPLY_CHARS = 220;
    private static final int MIN_CHAT_MAX_TOKENS = 180;
    private static final int MAX_CHAT_MAX_TOKENS = 480;

    private static final String PROMPT_VERSION = "practice-assistant-v1-step";

    private static final String CHAT_SYSTEM_PROMPT = """
            \u4f60\u662f\u4e2d\u5b66\u9898\u76ee\u5206\u6b65\u5f15\u5bfc\u52a9\u624b\u3002\u53ea\u7528\u7b80\u4f53\u4e2d\u6587\u56de\u7b54\uff0c\u53ea\u7ed9\u4e0b\u4e00\u6b65\uff0c\u4e0d\u7ed9\u6700\u7ec8\u7b54\u6848\uff0c\u6700\u540e\u7528\u4e00\u53e5\u95ee\u53e5\u5f15\u5bfc\u5b66\u751f\u7ee7\u7eed\u3002            """;

    private static final String CHAT_CONCISE_POLICY_PROMPT = """
            \u8f93\u51fa\u683c\u5f0f\uff1a\u7eaf\u6587\u672c\uff0c\u0032\u5230\u0034\u53e5\uff0c\u6700\u540e\u4e00\u53e5\u5fc5\u987b\u662f\u95ee\u53e5\u3002            """;

    private final PracticeSessionMapper practiceSessionMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final QuestionMapper questionMapper;
    private final PracticeQuestionAiChatSessionMapper practiceQuestionAiChatSessionMapper;
    private final PracticeQuestionAiChatMessageMapper practiceQuestionAiChatMessageMapper;
    private final QwenChatClient qwenChatClient;
    private final QwenChatOptionsResolver qwenChatOptionsResolver;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public PracticeQuestionAiServiceImpl(
            PracticeSessionMapper practiceSessionMapper,
            ExerciseRecordMapper exerciseRecordMapper,
            QuestionMapper questionMapper,
            PracticeQuestionAiChatSessionMapper practiceQuestionAiChatSessionMapper,
            PracticeQuestionAiChatMessageMapper practiceQuestionAiChatMessageMapper,
            QwenChatClient qwenChatClient,
            QwenChatOptionsResolver qwenChatOptionsResolver
    ) {
        this.practiceSessionMapper = practiceSessionMapper;
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.questionMapper = questionMapper;
        this.practiceQuestionAiChatSessionMapper = practiceQuestionAiChatSessionMapper;
        this.practiceQuestionAiChatMessageMapper = practiceQuestionAiChatMessageMapper;
        this.qwenChatClient = qwenChatClient;
        this.qwenChatOptionsResolver = qwenChatOptionsResolver;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public PracticeQuestionAiChatSessionVO getLatestChatSession(Long userId, Long practiceSessionId, Long questionId) {
        requirePracticeSession(userId, practiceSessionId);
        requireQuestionInSession(userId, practiceSessionId, questionId);

        PracticeQuestionAiChatSession session = findLatestAssistantSession(userId, practiceSessionId, questionId);
        if (session == null) {
            throw new BusinessException("\u5f53\u524d\u9898\u76ee\u6682\u65e0\u52a9\u624b\u4f1a\u8bdd", 404);
        }
        return toSessionVO(session);
    }

    /**
     * 执行创建业务流程并返回结果。
     */
    @Override
    @Transactional
    public PracticeQuestionAiChatSessionVO createChatSession(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            PracticeQuestionAiCreateSessionDTO requestDTO
    ) {
        PracticeSession practiceSession = requirePracticeSession(userId, practiceSessionId);
        if (!isSessionOngoing(practiceSession.getStatus())) {
            throw new BusinessException("\u5f53\u524d\u7ec3\u4e60\u5df2\u63d0\u4ea4\uff0c\u65e0\u6cd5\u521b\u5efa\u65b0\u4f1a\u8bdd", 400);
        }
        ExerciseRecord exerciseRecord = requireQuestionInSession(userId, practiceSessionId, questionId);
        Question question = requireQuestion(questionId);

        int triggerSource = normalizeTriggerSource(requestDTO == null ? null : requestDTO.getTriggerSource());
        PracticeQuestionAiChatSession existing = findLatestAssistantSession(userId, practiceSessionId, questionId);
        if (existing != null) {
            boolean shouldUpdate = false;
            if (!isSessionOngoing(existing.getStatus())) {
                existing.setStatus(SESSION_STATUS_ONGOING);
                shouldUpdate = true;
            }
            if (existing.getTriggerSource() == null) {
                existing.setTriggerSource(triggerSource);
                shouldUpdate = true;
            }
            if (shouldUpdate) {
                existing.setLastMessageAt(LocalDateTime.now());
                practiceQuestionAiChatSessionMapper.updateById(existing);
            }
            return toSessionVO(existing);
        }

        PracticeQuestionAiChatSession session = new PracticeQuestionAiChatSession();
        session.setSessionCode(newCode());
        session.setUserId(userId);
        session.setPracticeSessionId(practiceSessionId);
        session.setQuestionId(questionId);
        session.setSubjectId(question.getSubjectId());
        session.setExerciseRecordId(exerciseRecord.getId());
        session.setTriggerSource(triggerSource);
        session.setStatus(SESSION_STATUS_ONGOING);
        session.setRoundCount(0);
        session.setLastMessageAt(LocalDateTime.now());
        session.setTotalPromptTokens(0);
        session.setTotalCompletionTokens(0);
        session.setTotalTokens(0);
        practiceQuestionAiChatSessionMapper.insert(session);
        return toSessionVO(session);
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    @Override
    public List<PracticeQuestionAiChatMessageVO> listChatMessages(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId
    ) {
        requirePracticeSession(userId, practiceSessionId);
        requireQuestionInSession(userId, practiceSessionId, questionId);

        List<PracticeQuestionAiChatMessage> messages = practiceQuestionAiChatMessageMapper.selectList(
                new LambdaQueryWrapper<PracticeQuestionAiChatMessage>()
                        .inSql(
                                PracticeQuestionAiChatMessage::getAssistantSessionId,
                                "SELECT id FROM practice_question_ai_chat_session WHERE user_id = " + userId
                                        + " AND practice_session_id = " + practiceSessionId
                                        + " AND question_id = " + questionId
                                        + " AND deleted = 0"
                        )
                        .orderByAsc(PracticeQuestionAiChatMessage::getCreateTime)
                        .orderByAsc(PracticeQuestionAiChatMessage::getId)
        );
        return messages.stream().map(this::toMessageVO).toList();
    }

    /**
     * 执行核心业务处理流程。
     */
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public PracticeQuestionAiChatMessageVO sendChatMessage(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId,
            PracticeQuestionAiSendMessageDTO requestDTO
    ) {
        return doSendChatMessage(
                userId,
                practiceSessionId,
                questionId,
                assistantSessionId,
                requestDTO,
                null
        );
    }

    /**
     * 执行核心业务处理流程。
     */
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public PracticeQuestionAiChatMessageVO streamChatMessage(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId,
            PracticeQuestionAiSendMessageDTO requestDTO,
            Consumer<String> chunkConsumer
    ) {
        return doSendChatMessage(
                userId,
                practiceSessionId,
                questionId,
                assistantSessionId,
                requestDTO,
                chunkConsumer
        );
    }

    /**
     * 执行核心业务处理流程。
     */
    private PracticeQuestionAiChatMessageVO doSendChatMessage(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId,
            PracticeQuestionAiSendMessageDTO requestDTO,
            Consumer<String> chunkConsumer
    ) {
        PracticeSession practiceSession = requirePracticeSession(userId, practiceSessionId);
        if (!isSessionOngoing(practiceSession.getStatus())) {
            throw new BusinessException("\u5f53\u524d\u7ec3\u4e60\u5df2\u63d0\u4ea4\uff0c\u65e0\u6cd5\u7ee7\u7eed\u63d0\u95ee", 400);
        }
        PracticeQuestionAiChatSession assistantSession = requireAssistantSession(userId, practiceSessionId, questionId, assistantSessionId);
        if (!isSessionOngoing(assistantSession.getStatus())) {
            throw new BusinessException("\u5f53\u524d\u52a9\u624b\u4f1a\u8bdd\u5df2\u7ed3\u675f\uff0c\u8bf7\u65b0\u5efa\u4f1a\u8bdd", 400);
        }

        ExerciseRecord exerciseRecord = requireQuestionInSession(userId, practiceSessionId, questionId);
        Question question = requireQuestion(questionId);
        String content = normalizeInputContent(requestDTO.getContent());
        String draftAnswer = normalizeDraftAnswer(requestDTO.getDraftAnswer());
        int timeCost = normalizeTimeCost(requestDTO.getTimeCost());

        int nextSeq = nextSeqNo(assistantSessionId);
        PracticeQuestionAiChatMessage userMessage = new PracticeQuestionAiChatMessage();
        userMessage.setAssistantSessionId(assistantSessionId);
        userMessage.setSeqNo(nextSeq);
        userMessage.setRole(ROLE_USER);
        userMessage.setMessageType(MESSAGE_TYPE_TEXT);
        userMessage.setContentText(content);
        userMessage.setDraftAnswerSnapshot(draftAnswer);
        userMessage.setDraftTimeCostSnapshot(timeCost);
        userMessage.setStatus(RECORD_STATUS_SUCCESS);
        userMessage.setPromptTokens(0);
        userMessage.setCompletionTokens(0);
        userMessage.setTotalTokens(0);
        userMessage.setLatencyMs(0);
        practiceQuestionAiChatMessageMapper.insert(userMessage);

        JSONObject chatContext = buildChatContext(
                userId,
                practiceSession,
                question,
                exerciseRecord,
                assistantSession,
                draftAnswer,
                timeCost
        );
        List<QwenChatClient.ChatMessage> promptMessages = buildChatPromptMessages(chatContext, assistantSessionId);
        int chatMaxTokens = resolveChatMaxTokens();

        long beginMs = System.currentTimeMillis();
        try {
            QwenChatClient.ChatResult chatResult = chunkConsumer == null
                    ? qwenChatClient.chat(promptMessages, false, chatMaxTokens)
                    : qwenChatClient.chatStream(promptMessages, false, chatMaxTokens, chunkConsumer);
            int latencyMs = (int) (System.currentTimeMillis() - beginMs);
            String assistantContent = compactChatReply(chatResult.content(), MAX_CHAT_REPLY_CHARS);

            PracticeQuestionAiChatMessage assistantMessage = new PracticeQuestionAiChatMessage();
            assistantMessage.setAssistantSessionId(assistantSessionId);
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
            practiceQuestionAiChatMessageMapper.insert(assistantMessage);

            updateSessionAfterReply(assistantSession, chatResult.usage(), LocalDateTime.now(), true);
            return toMessageVO(assistantMessage);
        } catch (BusinessException exception) {
            persistFailedAssistantReply(assistantSessionId, nextSeq + 1, beginMs, exception.getMessage());
            updateSessionAfterReply(assistantSession, new QwenChatClient.Usage(0, 0, 0), LocalDateTime.now(), false);
            throw exception;
        } catch (Exception exception) {
            log.error("Practice assistant chat failed, userId={}, practiceSessionId={}, questionId={}",
                    userId, practiceSessionId, questionId, exception);
            persistFailedAssistantReply(assistantSessionId, nextSeq + 1, beginMs, "智能讲解失败，请稍后再试");
            updateSessionAfterReply(assistantSession, new QwenChatClient.Usage(0, 0, 0), LocalDateTime.now(), false);
            throw new BusinessException("智能讲解失败，请稍后再试", 502);
        }
    }
    /**
     * 执行删除与清理业务流程。
     */
    @Override
    @Transactional
    public PracticeQuestionAiChatSessionVO closeChatSession(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId
    ) {
        requirePracticeSession(userId, practiceSessionId);
        requireQuestionInSession(userId, practiceSessionId, questionId);
        PracticeQuestionAiChatSession session = requireAssistantSession(userId, practiceSessionId, questionId, assistantSessionId);
        if (isSessionClosed(session.getStatus())) {
            return toSessionVO(session);
        }
        if (!hasUserMessageContent(assistantSessionId)) {
            practiceQuestionAiChatSessionMapper.deleteById(assistantSessionId);
            session.setStatus(SESSION_STATUS_CLOSED);
            session.setLastMessageAt(LocalDateTime.now());
            return toSessionVO(session);
        }
        session.setStatus(SESSION_STATUS_CLOSED);
        session.setLastMessageAt(LocalDateTime.now());
        practiceQuestionAiChatSessionMapper.updateById(session);
        return toSessionVO(session);
    }

    /**
     * 构建业务处理所需数据。
     */
    private JSONObject buildChatContext(
            Long userId,
            PracticeSession practiceSession,
            Question question,
            ExerciseRecord exerciseRecord,
            PracticeQuestionAiChatSession assistantSession,
            String draftAnswer,
            int timeCost
    ) {
        JSONObject context = new JSONObject();
        context.put("userId", userId);
        context.put("practiceSessionId", practiceSession.getId());
        context.put("practiceMode", practiceSession.getPracticeMode());
        context.put("answeredCount", defaultNumber(practiceSession.getAnsweredCount()));
        context.put("questionCount", defaultNumber(practiceSession.getQuestionCount()));

        JSONObject questionNode = new JSONObject();
        questionNode.put("questionId", question.getId());
        questionNode.put("title", compactText(question.getTitle(), 120));
        questionNode.put("content", compactText(question.getContent(), MAX_QUESTION_CONTENT_CHARS));
        questionNode.put("type", question.getType());
        questionNode.put("options", parseJsonIfPossible(question.getOptions()));
        questionNode.put("difficulty", question.getDifficulty());
        questionNode.put("standardAnswerInternal", compactText(question.getAnswer(), MAX_INTERNAL_ANSWER_CHARS));
        questionNode.put("officialAnalysisInternal", compactText(question.getAnalysis(), MAX_INTERNAL_ANALYSIS_CHARS));
        context.put("question", questionNode);

        JSONObject userDraftNode = new JSONObject();
        userDraftNode.put("draftAnswer", compactText(draftAnswer, MAX_DRAFT_ANSWER_CHARS));
        userDraftNode.put("draftTimeCostSeconds", timeCost);
        userDraftNode.put("recordId", exerciseRecord.getId());
        context.put("userDraft", userDraftNode);

        context.put("assistantSessionCode", assistantSession.getSessionCode());
        context.put("promptVersion", PROMPT_VERSION);
        context.put("generatedAt", LocalDateTime.now().toString());
        return context;
    }

    /**
     * 构建业务处理所需数据。
     */
    private List<QwenChatClient.ChatMessage> buildChatPromptMessages(JSONObject context, Long assistantSessionId) {
        List<QwenChatClient.ChatMessage> messages = new ArrayList<>();
        messages.add(new QwenChatClient.ChatMessage("system", CHAT_SYSTEM_PROMPT));
        messages.add(new QwenChatClient.ChatMessage("system", CHAT_CONCISE_POLICY_PROMPT));
        messages.add(new QwenChatClient.ChatMessage("system", "Context(JSON): " + context.toJSONString()));

        List<PracticeQuestionAiChatMessage> historyMessages = practiceQuestionAiChatMessageMapper.selectList(
                new LambdaQueryWrapper<PracticeQuestionAiChatMessage>()
                        .eq(PracticeQuestionAiChatMessage::getAssistantSessionId, assistantSessionId)
                        .eq(PracticeQuestionAiChatMessage::getStatus, RECORD_STATUS_SUCCESS)
                        .orderByDesc(PracticeQuestionAiChatMessage::getSeqNo)
                        .last("LIMIT " + MAX_CONTEXT_MESSAGES)
        );
        historyMessages.sort(Comparator.comparing(PracticeQuestionAiChatMessage::getSeqNo));
        for (PracticeQuestionAiChatMessage history : historyMessages) {
            String role = mapRoleToOpenAiRole(history.getRole());
            if (!StringUtils.hasText(role) || !StringUtils.hasText(history.getContentText())) {
                continue;
            }
            messages.add(new QwenChatClient.ChatMessage(
                    role,
                    compactText(history.getContentText(), MAX_HISTORY_MESSAGE_CHARS)
            ));
        }
        return messages;
    }

    /**
     * 执行保存与更新业务流程。
     */
    private void persistFailedAssistantReply(Long assistantSessionId, int seqNo, long beginMs, String errorMessage) {
        PracticeQuestionAiChatMessage failedMessage = new PracticeQuestionAiChatMessage();
        failedMessage.setAssistantSessionId(assistantSessionId);
        failedMessage.setSeqNo(seqNo);
        failedMessage.setRole(ROLE_ASSISTANT);
        failedMessage.setMessageType(MESSAGE_TYPE_TEXT);
        failedMessage.setContentText(null);
        failedMessage.setModelProvider(qwenChatOptionsResolver.modelProvider());
        failedMessage.setModelName(qwenChatOptionsResolver.resolveChatModelName());
        failedMessage.setStatus(RECORD_STATUS_FAILED);
        failedMessage.setErrorMessage(errorMessage);
        failedMessage.setLatencyMs((int) (System.currentTimeMillis() - beginMs));
        practiceQuestionAiChatMessageMapper.insert(failedMessage);
    }

    /**
     * 执行保存与更新业务流程。
     */
    private void updateSessionAfterReply(
            PracticeQuestionAiChatSession session,
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
        practiceQuestionAiChatSessionMapper.updateById(session);
    }

    /**
     * 执行参数与状态校验。
     */
    private PracticeSession requirePracticeSession(Long userId, Long practiceSessionId) {
        PracticeSession practiceSession = practiceSessionMapper.selectOne(new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getId, practiceSessionId)
                .eq(PracticeSession::getUserId, userId)
                .last("LIMIT 1"));
        if (practiceSession == null) {
            throw new BusinessException("\u7ec3\u4e60\u4f1a\u8bdd\u4e0d\u5b58\u5728\u6216\u65e0\u8bbf\u95ee\u6743\u9650", 404);
        }
        return practiceSession;
    }

    /**
     * 执行参数与状态校验。
     */
    private ExerciseRecord requireQuestionInSession(Long userId, Long practiceSessionId, Long questionId) {
        ExerciseRecord exerciseRecord = exerciseRecordMapper.selectOne(new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, userId)
                .eq(ExerciseRecord::getSessionRefId, practiceSessionId)
                .eq(ExerciseRecord::getQuestionId, questionId)
                .last("LIMIT 1"));
        if (exerciseRecord == null) {
            throw new BusinessException("\u9898\u76ee\u4e0d\u5c5e\u4e8e\u5f53\u524d\u7ec3\u4e60\u4f1a\u8bdd", 404);
        }
        return exerciseRecord;
    }

    /**
     * 执行参数与状态校验。
     */
    private Question requireQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || Integer.valueOf(1).equals(question.getDeleted())) {
            throw new BusinessException("\u9898\u76ee\u4e0d\u5b58\u5728\u6216\u5df2\u5220\u9664", 404);
        }
        return question;
    }

    /**
     * 执行参数与状态校验。
     */
    private PracticeQuestionAiChatSession requireAssistantSession(
            Long userId,
            Long practiceSessionId,
            Long questionId,
            Long assistantSessionId
    ) {
        PracticeQuestionAiChatSession session = practiceQuestionAiChatSessionMapper.selectOne(
                new LambdaQueryWrapper<PracticeQuestionAiChatSession>()
                        .eq(PracticeQuestionAiChatSession::getId, assistantSessionId)
                        .eq(PracticeQuestionAiChatSession::getUserId, userId)
                        .eq(PracticeQuestionAiChatSession::getPracticeSessionId, practiceSessionId)
                        .eq(PracticeQuestionAiChatSession::getQuestionId, questionId)
                        .last("LIMIT 1")
        );
        if (session == null) {
            throw new BusinessException("\u52a9\u624b\u4f1a\u8bdd\u4e0d\u5b58\u5728\u6216\u65e0\u8bbf\u95ee\u6743\u9650", 404);
        }
        return session;
    }

    /**
     * 执行查询业务流程并返回结果。
     */
    private PracticeQuestionAiChatSession findLatestAssistantSession(
            Long userId,
            Long practiceSessionId,
            Long questionId
    ) {
        return practiceQuestionAiChatSessionMapper.selectOne(
                new LambdaQueryWrapper<PracticeQuestionAiChatSession>()
                        .eq(PracticeQuestionAiChatSession::getUserId, userId)
                        .eq(PracticeQuestionAiChatSession::getPracticeSessionId, practiceSessionId)
                        .eq(PracticeQuestionAiChatSession::getQuestionId, questionId)
                        .orderByDesc(PracticeQuestionAiChatSession::getLastMessageAt)
                        .orderByDesc(PracticeQuestionAiChatSession::getId)
                        .last("LIMIT 1")
        );
    }

    /**
     * 计算并返回处理结果。
     */
    private int nextSeqNo(Long assistantSessionId) {
        PracticeQuestionAiChatMessage latest = practiceQuestionAiChatMessageMapper.selectOne(
                new LambdaQueryWrapper<PracticeQuestionAiChatMessage>()
                        .eq(PracticeQuestionAiChatMessage::getAssistantSessionId, assistantSessionId)
                        .orderByDesc(PracticeQuestionAiChatMessage::getSeqNo)
                        .orderByDesc(PracticeQuestionAiChatMessage::getId)
                        .last("LIMIT 1")
        );
        return latest == null ? 1 : defaultNumber(latest.getSeqNo()) + 1;
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean hasUserMessageContent(Long assistantSessionId) {
        Number count = practiceQuestionAiChatMessageMapper.selectCount(
                new LambdaQueryWrapper<PracticeQuestionAiChatMessage>()
                        .eq(PracticeQuestionAiChatMessage::getAssistantSessionId, assistantSessionId)
                        .eq(PracticeQuestionAiChatMessage::getRole, ROLE_USER)
        );
        return count != null && count.longValue() > 0;
    }

    /**
     * 解析并转换输入数据。
     */
    private int normalizeTriggerSource(Integer triggerSource) {
        if (triggerSource == null) {
            return SESSION_TRIGGER_SOURCE_MANUAL;
        }
        if (triggerSource == SESSION_TRIGGER_SOURCE_MANUAL || triggerSource == SESSION_TRIGGER_SOURCE_AUTO) {
            return triggerSource;
        }
        return SESSION_TRIGGER_SOURCE_MANUAL;
    }

    /**
     * 解析并转换输入数据。
     */
    private int normalizeTimeCost(Integer timeCost) {
        if (timeCost == null) {
            return 0;
        }
        return Math.max(timeCost, 0);
    }

    /**
     * 解析并转换输入数据。
     */
    private String normalizeInputContent(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("输入内容不能为空", 400);
        }
        return normalized;
    }

    /**
     * 解析并转换输入数据。
     */
    private String normalizeDraftAnswer(String draftAnswer) {
        if (!StringUtils.hasText(draftAnswer)) {
            return "";
        }
        String normalized = draftAnswer.trim().replaceAll("\\s+", " ");
        return compactText(normalized, 4000);
    }

    /**
     * 执行核心业务处理流程。
     */
    private String compactChatReply(String text, int maxChars) {
        if (!StringUtils.hasText(text)) {
            return "\u6211\u4eec\u5148\u4ece\u9898\u5e72\u6761\u4ef6\u91cc\u627e\u4e00\u4e2a\u53ef\u4ee5\u76f4\u63a5\u8ba1\u7b97\u7684\u91cf\uff0c\u4f60\u51c6\u5907\u5148\u5199\u4e0b\u54ea\u4e00\u4e2a\uff1f";
        }
        String normalized = sanitizeAssistantReplyText(text)
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .replaceAll("(?m)^[-*]\\s+", "")
                .replaceAll("(?m)^\\d+\\.\\s+", "")
                .trim()
                .replaceAll("\\s+", " ");
        if (!StringUtils.hasText(normalized)) {
            normalized = sanitizeAssistantReplyText(text).trim().replaceAll("\\s+", " ");
        }
        if (normalized.length() > maxChars) {
            normalized = normalized.substring(0, maxChars);
        }
        if (!normalized.endsWith("？") && !normalized.endsWith("?")) {
            normalized = normalized + "\u0020\u4f60\u51c6\u5907\u5148\u5c1d\u8bd5\u54ea\u4e00\u6b65\uff1f";
        }
        return normalized;
    }

    /**
     * 执行核心业务处理流程。
     */
    private String sanitizeAssistantReplyText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text;
        normalized = normalized.replaceAll("\\$\\$([\\s\\S]*?)\\$\\$", "$1");
        normalized = normalized.replaceAll("\\$([^$\\n]+)\\$", "$1");
        normalized = normalized.replaceAll("\\\\vec\\s*\\{\\s*([^{}]+)\\s*\\}", "vector $1");
        normalized = normalized.replaceAll("\\\\overrightarrow\\s*\\{\\s*([^{}]+)\\s*\\}", "vector $1");
        normalized = normalized.replaceAll("\\\\frac\\s*\\{\\s*([^{}]+)\\s*\\}\\s*\\{\\s*([^{}]+)\\s*\\}", "$1/$2");
        normalized = normalized.replaceAll("\\\\times", "*");
        normalized = normalized.replaceAll("\\\\cdot", "*");
        normalized = normalized.replaceAll("\\\\div", "/");
        normalized = normalized.replaceAll("\\\\leq?", "<=");
        normalized = normalized.replaceAll("\\\\geq?", ">=");
        normalized = normalized.replaceAll("\\\\neq", "!=");
        normalized = normalized.replaceAll("\\\\pm", "+/-");
        normalized = normalized.replaceAll("\\\\angle\\s*", "angle ");
        normalized = normalized.replaceAll("\\\\triangle\\s*", "triangle ");
        normalized = normalized.replaceAll("[{}]", "");
        normalized = normalized.replace("`", "");
        normalized = normalized.replace("\\", "");
        return normalized;
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
    private PracticeQuestionAiChatSessionVO toSessionVO(PracticeQuestionAiChatSession session) {
        PracticeQuestionAiChatSessionVO vo = new PracticeQuestionAiChatSessionVO();
        vo.setSessionId(session.getId());
        vo.setSessionCode(session.getSessionCode());
        vo.setPracticeSessionId(session.getPracticeSessionId());
        vo.setQuestionId(session.getQuestionId());
        vo.setSubjectId(session.getSubjectId());
        vo.setTriggerSource(session.getTriggerSource());
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
    private PracticeQuestionAiChatMessageVO toMessageVO(PracticeQuestionAiChatMessage message) {
        PracticeQuestionAiChatMessageVO vo = new PracticeQuestionAiChatMessageVO();
        vo.setMessageId(message.getId());
        vo.setSessionId(message.getAssistantSessionId());
        vo.setSeqNo(message.getSeqNo());
        vo.setRole(message.getRole());
        vo.setRoleName(mapRoleName(message.getRole()));
        vo.setContent(message.getContentText());
        vo.setDraftAnswerSnapshot(message.getDraftAnswerSnapshot());
        vo.setDraftTimeCostSnapshot(message.getDraftTimeCostSnapshot());
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
    private int resolveChatMaxTokens() {
        return qwenChatOptionsResolver.resolveBoundedChatMaxTokens(
                MIN_CHAT_MAX_TOKENS,
                MAX_CHAT_MAX_TOKENS
        );
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean isSessionOngoing(Integer status) {
        return status != null && status == PracticeModeConstants.STATUS_ONGOING;
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean isSessionClosed(Integer status) {
        return status != null && status == SESSION_STATUS_CLOSED;
    }
}
