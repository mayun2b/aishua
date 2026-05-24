package zysy.iflytek.aishua.modules.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.config.properties.QwenAiProperties;
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
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
public class PracticeQuestionAiServiceImpl implements PracticeQuestionAiService {
    private static final int ROLE_SYSTEM = 1;
    private static final int ROLE_USER = 2;
    private static final int ROLE_ASSISTANT = 3;

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

    private static final String MODEL_PROVIDER = "qwen";
    private static final String PROMPT_VERSION = "practice-assistant-v1-step";

    private static final String CHAT_SYSTEM_PROMPT = """
            你是一个中学题目分步引导助手。
            严格规则：
            1) 只能使用简体中文回复。
            2) 每次只给“下一步”，不要直接给最终答案、最终选项或完整推导。
            3) 你可以内部参考标准答案，但绝不能直接泄露答案。
            4) 回复要简短、可执行、以启发为主。
            5) 结尾必须用一句简短问题引导学生继续尝试。
            6) 禁止使用 LaTeX / Markdown 数学写法（例如 $...$、\\vec{}、\\frac{}、^、_ 等）；请改用自然中文表达。
            """;

    private static final String CHAT_CONCISE_POLICY_PROMPT = """
            输出格式约束：
            - 纯文本，不要 markdown 标题、列表、代码块。
            - 总共 2 到 4 句短句。
            - 最后一句必须是问句。
            """;

    private final PracticeSessionMapper practiceSessionMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final QuestionMapper questionMapper;
    private final PracticeQuestionAiChatSessionMapper practiceQuestionAiChatSessionMapper;
    private final PracticeQuestionAiChatMessageMapper practiceQuestionAiChatMessageMapper;
    private final QwenChatClient qwenChatClient;
    private final QwenAiProperties qwenAiProperties;

    public PracticeQuestionAiServiceImpl(
            PracticeSessionMapper practiceSessionMapper,
            ExerciseRecordMapper exerciseRecordMapper,
            QuestionMapper questionMapper,
            PracticeQuestionAiChatSessionMapper practiceQuestionAiChatSessionMapper,
            PracticeQuestionAiChatMessageMapper practiceQuestionAiChatMessageMapper,
            QwenChatClient qwenChatClient,
            QwenAiProperties qwenAiProperties
    ) {
        this.practiceSessionMapper = practiceSessionMapper;
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.questionMapper = questionMapper;
        this.practiceQuestionAiChatSessionMapper = practiceQuestionAiChatSessionMapper;
        this.practiceQuestionAiChatMessageMapper = practiceQuestionAiChatMessageMapper;
        this.qwenChatClient = qwenChatClient;
        this.qwenAiProperties = qwenAiProperties;
    }

    @Override
    public PracticeQuestionAiChatSessionVO getLatestChatSession(Long userId, Long practiceSessionId, Long questionId) {
        requirePracticeSession(userId, practiceSessionId);
        requireQuestionInSession(userId, practiceSessionId, questionId);

        PracticeQuestionAiChatSession session = findLatestAssistantSession(userId, practiceSessionId, questionId);
        if (session == null) {
            throw new BusinessException("当前题目暂无助手会话", 404);
        }
        return toSessionVO(session);
    }

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
            throw new BusinessException("当前练习已提交，无法创建新会话", 400);
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
            throw new BusinessException("当前练习已提交，无法继续提问", 400);
        }
        PracticeQuestionAiChatSession assistantSession = requireAssistantSession(userId, practiceSessionId, questionId, assistantSessionId);
        if (!isSessionOngoing(assistantSession.getStatus())) {
            throw new BusinessException("当前助手会话已结束，请新建会话", 400);
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

        long beginMs = System.currentTimeMillis();
        try {
            QwenChatClient.ChatResult chatResult = chunkConsumer == null
                    ? qwenChatClient.chat(promptMessages, false, resolveChatMaxTokens())
                    : qwenChatClient.chatStream(promptMessages, false, resolveChatMaxTokens(), chunkConsumer);
            int latencyMs = (int) (System.currentTimeMillis() - beginMs);
            String assistantContent = compactChatReply(chatResult.content(), MAX_CHAT_REPLY_CHARS);

            PracticeQuestionAiChatMessage assistantMessage = new PracticeQuestionAiChatMessage();
            assistantMessage.setAssistantSessionId(assistantSessionId);
            assistantMessage.setSeqNo(nextSeq + 1);
            assistantMessage.setRole(ROLE_ASSISTANT);
            assistantMessage.setMessageType(MESSAGE_TYPE_TEXT);
            assistantMessage.setContentText(assistantContent);
            assistantMessage.setModelProvider(MODEL_PROVIDER);
            assistantMessage.setModelName(resolveChatModelName());
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
            persistFailedAssistantReply(assistantSessionId, nextSeq + 1, beginMs, "AI 讲解失败，请稍后再试");
            updateSessionAfterReply(assistantSession, new QwenChatClient.Usage(0, 0, 0), LocalDateTime.now(), false);
            throw new BusinessException("AI 讲解失败，请稍后再试", 502);
        }
    }
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

    private void persistFailedAssistantReply(Long assistantSessionId, int seqNo, long beginMs, String errorMessage) {
        PracticeQuestionAiChatMessage failedMessage = new PracticeQuestionAiChatMessage();
        failedMessage.setAssistantSessionId(assistantSessionId);
        failedMessage.setSeqNo(seqNo);
        failedMessage.setRole(ROLE_ASSISTANT);
        failedMessage.setMessageType(MESSAGE_TYPE_TEXT);
        failedMessage.setContentText(null);
        failedMessage.setModelProvider(MODEL_PROVIDER);
        failedMessage.setModelName(resolveChatModelName());
        failedMessage.setStatus(RECORD_STATUS_FAILED);
        failedMessage.setErrorMessage(errorMessage);
        failedMessage.setLatencyMs((int) (System.currentTimeMillis() - beginMs));
        practiceQuestionAiChatMessageMapper.insert(failedMessage);
    }

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

    private PracticeSession requirePracticeSession(Long userId, Long practiceSessionId) {
        PracticeSession practiceSession = practiceSessionMapper.selectOne(new LambdaQueryWrapper<PracticeSession>()
                .eq(PracticeSession::getId, practiceSessionId)
                .eq(PracticeSession::getUserId, userId)
                .last("LIMIT 1"));
        if (practiceSession == null) {
            throw new BusinessException("练习会话不存在或无访问权限", 404);
        }
        return practiceSession;
    }

    private ExerciseRecord requireQuestionInSession(Long userId, Long practiceSessionId, Long questionId) {
        ExerciseRecord exerciseRecord = exerciseRecordMapper.selectOne(new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, userId)
                .eq(ExerciseRecord::getSessionRefId, practiceSessionId)
                .eq(ExerciseRecord::getQuestionId, questionId)
                .last("LIMIT 1"));
        if (exerciseRecord == null) {
            throw new BusinessException("题目不属于当前练习会话", 404);
        }
        return exerciseRecord;
    }

    private Question requireQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || Integer.valueOf(1).equals(question.getDeleted())) {
            throw new BusinessException("题目不存在或已删除", 404);
        }
        return question;
    }

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
            throw new BusinessException("助手会话不存在或无访问权限", 404);
        }
        return session;
    }

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

    private boolean hasUserMessageContent(Long assistantSessionId) {
        Number count = practiceQuestionAiChatMessageMapper.selectCount(
                new LambdaQueryWrapper<PracticeQuestionAiChatMessage>()
                        .eq(PracticeQuestionAiChatMessage::getAssistantSessionId, assistantSessionId)
                        .eq(PracticeQuestionAiChatMessage::getRole, ROLE_USER)
        );
        return count != null && count.longValue() > 0;
    }

    private int normalizeTriggerSource(Integer triggerSource) {
        if (triggerSource == null) {
            return SESSION_TRIGGER_SOURCE_MANUAL;
        }
        if (triggerSource == SESSION_TRIGGER_SOURCE_MANUAL || triggerSource == SESSION_TRIGGER_SOURCE_AUTO) {
            return triggerSource;
        }
        return SESSION_TRIGGER_SOURCE_MANUAL;
    }

    private int normalizeTimeCost(Integer timeCost) {
        if (timeCost == null) {
            return 0;
        }
        return Math.max(timeCost, 0);
    }

    private String normalizeInputContent(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("提问内容不能为空", 400);
        }
        return normalized;
    }

    private String normalizeDraftAnswer(String draftAnswer) {
        if (!StringUtils.hasText(draftAnswer)) {
            return "";
        }
        String normalized = draftAnswer.trim().replaceAll("\\s+", " ");
        return compactText(normalized, 4000);
    }

    private String compactChatReply(String text, int maxChars) {
        if (!StringUtils.hasText(text)) {
            return "我们先从题干条件里找一个可以直接计算的量，你准备先写下哪一个？";
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
            normalized = normalized + " 你准备先尝试哪一步？";
        }
        return normalized;
    }

    private String sanitizeAssistantReplyText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text;
        normalized = normalized.replaceAll("\\$\\$([\\s\\S]*?)\\$\\$", "$1");
        normalized = normalized.replaceAll("\\$([^$\\n]+)\\$", "$1");
        normalized = normalized.replaceAll("\\\\vec\\s*\\{\\s*([^{}]+)\\s*\\}", "向量$1");
        normalized = normalized.replaceAll("\\\\overrightarrow\\s*\\{\\s*([^{}]+)\\s*\\}", "向量$1");
        normalized = normalized.replaceAll("\\\\frac\\s*\\{\\s*([^{}]+)\\s*\\}\\s*\\{\\s*([^{}]+)\\s*\\}", "$1/$2");
        normalized = normalized.replaceAll("\\\\times", "×");
        normalized = normalized.replaceAll("\\\\cdot", "·");
        normalized = normalized.replaceAll("\\\\div", "÷");
        normalized = normalized.replaceAll("\\\\leq?", "≤");
        normalized = normalized.replaceAll("\\\\geq?", "≥");
        normalized = normalized.replaceAll("\\\\neq", "≠");
        normalized = normalized.replaceAll("\\\\pm", "±");
        normalized = normalized.replaceAll("\\\\angle\\s*", "角");
        normalized = normalized.replaceAll("\\\\triangle\\s*", "三角形");
        normalized = normalized.replaceAll("[{}]", "");
        normalized = normalized.replace("`", "");
        normalized = normalized.replace("\\", "");
        return normalized;
    }

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

    private Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private String mapRoleToOpenAiRole(Integer role) {
        if (ROLE_SYSTEM == role) {
            return "system";
        }
        if (ROLE_USER == role) {
            return "user";
        }
        if (ROLE_ASSISTANT == role) {
            return "assistant";
        }
        return null;
    }

    private String mapRoleName(Integer role) {
        if (ROLE_SYSTEM == role) {
            return "system";
        }
        if (ROLE_USER == role) {
            return "user";
        }
        if (ROLE_ASSISTANT == role) {
            return "assistant";
        }
        return "unknown";
    }

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

    private String newCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveChatModelName() {
        if (StringUtils.hasText(qwenAiProperties.getChatModel())) {
            return qwenAiProperties.getChatModel().trim();
        }
        return "qwen3.5-flash";
    }

    private int resolveChatMaxTokens() {
        int configured = qwenAiProperties.getChatMaxTokens() == null ? 220 : qwenAiProperties.getChatMaxTokens();
        int safe = Math.max(configured, MIN_CHAT_MAX_TOKENS);
        return Math.min(safe, MAX_CHAT_MAX_TOKENS);
    }

    private boolean isSessionOngoing(Integer status) {
        return status != null && status == PracticeModeConstants.STATUS_ONGOING;
    }

    private boolean isSessionClosed(Integer status) {
        return status != null && status == SESSION_STATUS_CLOSED;
    }
}
