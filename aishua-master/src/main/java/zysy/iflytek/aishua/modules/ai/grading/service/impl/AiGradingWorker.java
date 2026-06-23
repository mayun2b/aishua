package zysy.iflytek.aishua.modules.ai.grading.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.common.minio.service.MinioService;
import zysy.iflytek.aishua.config.properties.QwenAiProperties;
import zysy.iflytek.aishua.modules.ai.grading.entity.AiGradingTask;
import zysy.iflytek.aishua.modules.ai.grading.mapper.AiGradingTaskMapper;
import zysy.iflytek.aishua.modules.ai.grading.support.AnswerPayloadParser;
import zysy.iflytek.aishua.modules.ai.grading.support.QwenGradingClient;
import zysy.iflytek.aishua.modules.exam.entity.ExamRecord;
import zysy.iflytek.aishua.modules.exam.entity.ExamRecordQuestion;
import zysy.iflytek.aishua.modules.exam.mapper.ExamRecordMapper;
import zysy.iflytek.aishua.modules.exam.mapper.ExamRecordQuestionMapper;
import zysy.iflytek.aishua.modules.practice.entity.ExerciseRecord;
import zysy.iflytek.aishua.modules.practice.entity.PracticeSession;
import zysy.iflytek.aishua.modules.practice.entity.WrongQuestion;
import zysy.iflytek.aishua.modules.practice.mapper.ExerciseRecordMapper;
import zysy.iflytek.aishua.modules.practice.mapper.PracticeSessionMapper;
import zysy.iflytek.aishua.modules.practice.mapper.WrongQuestionMapper;
import zysy.iflytek.aishua.modules.practice.support.PracticeSubmissionStatsUpdater;
import zysy.iflytek.aishua.modules.question.entity.Question;
import zysy.iflytek.aishua.modules.question.entity.QuestionTagRelation;
import zysy.iflytek.aishua.modules.question.mapper.QuestionMapper;
import zysy.iflytek.aishua.modules.question.mapper.QuestionTagRelationMapper;
import zysy.iflytek.aishua.modules.tag.entity.ExamTag;
import zysy.iflytek.aishua.modules.tag.mapper.ExamTagMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.BIZ_TYPE_EXAM;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.BIZ_TYPE_PRACTICE;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.STATUS_FAILED;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.STATUS_NOT_REQUIRED;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.STATUS_PARTIAL_FAILED;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.STATUS_PENDING;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.STATUS_PROCESSING;
import static zysy.iflytek.aishua.modules.ai.grading.support.AiGradingConstants.STATUS_SUCCESS;

/**
 * 应用内 AI 评分 Worker。
 */
@Slf4j
@Service
public class AiGradingWorker {
    private static final int DEFAULT_BATCH_SIZE = 3;
    private static final int DEFAULT_MAX_IMAGES = 6;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final AiGradingTaskMapper aiGradingTaskMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final PracticeSessionMapper practiceSessionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamRecordQuestionMapper examRecordQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionTagRelationMapper questionTagRelationMapper;
    private final ExamTagMapper examTagMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final PracticeSubmissionStatsUpdater practiceSubmissionStatsUpdater;
    private final AnswerPayloadParser answerPayloadParser;
    private final QwenGradingClient qwenGradingClient;
    private final MinioService minioService;
    private final QwenAiProperties qwenAiProperties;
    private final TransactionTemplate transactionTemplate;

    public AiGradingWorker(
            AiGradingTaskMapper aiGradingTaskMapper,
            ExerciseRecordMapper exerciseRecordMapper,
            PracticeSessionMapper practiceSessionMapper,
            ExamRecordMapper examRecordMapper,
            ExamRecordQuestionMapper examRecordQuestionMapper,
            QuestionMapper questionMapper,
            QuestionTagRelationMapper questionTagRelationMapper,
            ExamTagMapper examTagMapper,
            WrongQuestionMapper wrongQuestionMapper,
            PracticeSubmissionStatsUpdater practiceSubmissionStatsUpdater,
            AnswerPayloadParser answerPayloadParser,
            QwenGradingClient qwenGradingClient,
            MinioService minioService,
            QwenAiProperties qwenAiProperties,
            TransactionTemplate transactionTemplate
    ) {
        this.aiGradingTaskMapper = aiGradingTaskMapper;
        this.exerciseRecordMapper = exerciseRecordMapper;
        this.practiceSessionMapper = practiceSessionMapper;
        this.examRecordMapper = examRecordMapper;
        this.examRecordQuestionMapper = examRecordQuestionMapper;
        this.questionMapper = questionMapper;
        this.questionTagRelationMapper = questionTagRelationMapper;
        this.examTagMapper = examTagMapper;
        this.wrongQuestionMapper = wrongQuestionMapper;
        this.practiceSubmissionStatsUpdater = practiceSubmissionStatsUpdater;
        this.answerPayloadParser = answerPayloadParser;
        this.qwenGradingClient = qwenGradingClient;
        this.minioService = minioService;
        this.qwenAiProperties = qwenAiProperties;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(
            fixedDelayString = "${ai.qwen.grading-worker-delay-ms:5000}",
            initialDelayString = "${ai.qwen.grading-worker-initial-delay-ms:8000}"
    )
    public void processPendingTasks() {
        List<AiGradingTask> tasks = aiGradingTaskMapper.selectList(new LambdaQueryWrapper<AiGradingTask>()
                .eq(AiGradingTask::getStatus, STATUS_PENDING)
                .orderByAsc(AiGradingTask::getId)
                .last("LIMIT " + resolveBatchSize()));
        for (AiGradingTask task : tasks) {
            processTask(task.getId());
        }
    }

    private void processTask(Long taskId) {
        LocalDateTime startedAt = LocalDateTime.now();
        if (aiGradingTaskMapper.markProcessing(taskId, startedAt) <= 0) {
            return;
        }

        AiGradingTask task = aiGradingTaskMapper.selectById(taskId);
        try {
            GradingAttempt attempt = buildAttempt(task);
            persistRequestPayload(task.getId(), attempt.auditPayload());
            GradingResult gradingResult = gradeAttempt(attempt);
            transactionTemplate.executeWithoutResult(status -> writeSuccess(task.getId(), gradingResult));
        } catch (Exception exception) {
            log.warn("AI 评分任务失败，taskId={}, reason={}", taskId, exception.getMessage());
            transactionTemplate.executeWithoutResult(status -> writeFailure(taskId, exception));
        }
    }

    private GradingAttempt buildAttempt(AiGradingTask task) {
        if (BIZ_TYPE_PRACTICE.equals(task.getBizType())) {
            return buildPracticeAttempt(task);
        }
        if (BIZ_TYPE_EXAM.equals(task.getBizType())) {
            return buildExamAttempt(task);
        }
        throw new BusinessException("不支持的 AI 评分任务类型：" + task.getBizType(), 500);
    }

    private GradingAttempt buildPracticeAttempt(AiGradingTask task) {
        ExerciseRecord record = requireExerciseRecord(task.getBizRecordId());
        Question question = requireQuestion(record.getQuestionId());
        AnswerPayloadParser.ParsedAnswer parsedAnswer = answerPayloadParser.parse(record.getUserAnswer());
        ImageBundle imageBundle = loadImages(parsedAnswer, question);
        JSONObject auditPayload = buildAuditPayload(task, question, parsedAnswer, imageBundle.auditImages(), null);
        return new GradingAttempt(task, record, null, question, parsedAnswer, imageBundle.images(), auditPayload, null);
    }

    private GradingAttempt buildExamAttempt(AiGradingTask task) {
        ExamRecordQuestion detail = requireExamRecordQuestion(task.getBizRecordId());
        Question question = requireQuestion(detail.getQuestionId());
        AnswerPayloadParser.ParsedAnswer parsedAnswer = answerPayloadParser.parse(detail.getUserAnswer());
        ImageBundle imageBundle = loadImages(parsedAnswer, question);
        Double fullScore = detail.getFullScore() == null ? 0d : detail.getFullScore();
        JSONObject auditPayload = buildAuditPayload(task, question, parsedAnswer, imageBundle.auditImages(), fullScore);
        return new GradingAttempt(task, null, detail, question, parsedAnswer, imageBundle.images(), auditPayload, fullScore);
    }

    private GradingResult gradeAttempt(GradingAttempt attempt) {
        if (!attempt.parsedAnswer().hasEvidence()) {
            return buildEmptyAnswerResult(attempt);
        }

        String systemPrompt = buildSystemPrompt(attempt);
        String userPrompt = buildUserPrompt(attempt);
        QwenGradingClient.GradingRawResult rawResult = qwenGradingClient.grade(systemPrompt, userPrompt, attempt.images());
        JSONObject responseJson = parseJsonObject(rawResult.content());
        if (BIZ_TYPE_PRACTICE.equals(attempt.task().getBizType())) {
            boolean isCorrect = Boolean.TRUE.equals(responseJson.getBoolean("isCorrect"));
            return new GradingResult(
                    attempt.task(),
                    isCorrect,
                    null,
                    clampConfidence(responseJson.getDouble("confidence")),
                    normalizeText(responseJson.getString("feedback")),
                    responseJson.toJSONString(),
                    rawResult.rawResponse()
            );
        }

        double fullScore = attempt.fullScore() == null ? 0d : attempt.fullScore();
        double awardedScore = clampScore(responseJson.getDouble("awardedScore"), fullScore);
        boolean isCorrect = fullScore > 0 && BigDecimal.valueOf(awardedScore).compareTo(BigDecimal.valueOf(fullScore)) == 0;
        return new GradingResult(
                attempt.task(),
                isCorrect,
                awardedScore,
                clampConfidence(responseJson.getDouble("confidence")),
                normalizeText(responseJson.getString("feedback")),
                responseJson.toJSONString(),
                rawResult.rawResponse()
        );
    }

    private GradingResult buildEmptyAnswerResult(GradingAttempt attempt) {
        JSONObject response = new JSONObject();
        response.put("confidence", 1);
        response.put("feedback", "未检测到有效作答内容。");
        response.put("reasoningSummary", "学生未提交文字、手写画布或题图标注。");
        if (BIZ_TYPE_PRACTICE.equals(attempt.task().getBizType())) {
            response.put("isCorrect", false);
            return new GradingResult(attempt.task(), false, null, 1d, response.getString("feedback"), response.toJSONString(), response.toJSONString());
        }
        response.put("awardedScore", 0);
        response.put("fullScore", attempt.fullScore());
        return new GradingResult(attempt.task(), false, 0d, 1d, response.getString("feedback"), response.toJSONString(), response.toJSONString());
    }

    private void writeSuccess(Long taskId, GradingResult result) {
        AiGradingTask latestTask = aiGradingTaskMapper.selectById(taskId);
        if (latestTask == null || STATUS_SUCCESS.equals(latestTask.getStatus())) {
            return;
        }
        if (BIZ_TYPE_PRACTICE.equals(latestTask.getBizType())) {
            writePracticeSuccess(latestTask, result);
        } else if (BIZ_TYPE_EXAM.equals(latestTask.getBizType())) {
            writeExamSuccess(latestTask, result);
        }

        latestTask.setStatus(STATUS_SUCCESS);
        latestTask.setResponsePayloadJson(buildTaskResponsePayload(result));
        latestTask.setErrorMessage(null);
        latestTask.setFinishedAt(LocalDateTime.now());
        aiGradingTaskMapper.updateById(latestTask);
    }

    private void writePracticeSuccess(AiGradingTask task, GradingResult result) {
        ExerciseRecord record = exerciseRecordMapper.selectById(task.getBizRecordId());
        if (record == null || STATUS_SUCCESS.equals(record.getAiGradingStatus())) {
            return;
        }

        Question question = requireQuestion(record.getQuestionId());
        PracticeSession session = record.getSessionRefId() == null ? null : practiceSessionMapper.selectById(record.getSessionRefId());
        record.setIsCorrect(result.isCorrect() ? 1 : 0);
        record.setAiGradingStatus(STATUS_SUCCESS);
        record.setAiGradingConfidence(result.confidence());
        record.setAiGradingFeedback(result.feedback());
        record.setAiGradingDetailJson(result.responsePayloadJson());
        record.setAiGradingErrorMessage(null);
        record.setAiGradedAt(LocalDateTime.now());
        exerciseRecordMapper.updateById(record);

        TagSnapshot tagSnapshot = loadTagSnapshot(question.getId());
        practiceSubmissionStatsUpdater.applyAnswerStats(
                record.getUserId(),
                session == null ? question.getSubjectId() : session.getSubjectId(),
                question,
                tagSnapshot.tagIds(),
                result.isCorrect(),
                defaultNumber(record.getTimeCost())
        );
        if (!result.isCorrect()) {
            upsertWrongQuestion(record.getUserId(), question, tagSnapshot.tagNames(), LocalDateTime.now());
        }
        updatePracticeSessionAfterSubjectiveResult(session, result.isCorrect(), false);
    }

    private void writeExamSuccess(AiGradingTask task, GradingResult result) {
        ExamRecordQuestion detail = examRecordQuestionMapper.selectById(task.getBizRecordId());
        if (detail == null) {
            return;
        }
        detail.setIsCorrect(result.isCorrect() ? 1 : 0);
        detail.setAwardedScore(result.awardedScore());
        detail.setAiGradingStatus(STATUS_SUCCESS);
        detail.setAiGradingConfidence(result.confidence());
        detail.setAiGradingFeedback(result.feedback());
        detail.setAiGradingDetailJson(result.responsePayloadJson());
        detail.setAiGradingErrorMessage(null);
        detail.setAiGradedAt(LocalDateTime.now());
        examRecordQuestionMapper.updateById(detail);
        recalculateExamRecordScore(detail.getExamRecordId());
    }

    private void writeFailure(Long taskId, Exception exception) {
        AiGradingTask task = aiGradingTaskMapper.selectById(taskId);
        if (task == null || STATUS_SUCCESS.equals(task.getStatus())) {
            return;
        }
        String errorMessage = truncateError(exception.getMessage());
        task.setStatus(STATUS_FAILED);
        task.setErrorMessage(errorMessage);
        task.setFinishedAt(LocalDateTime.now());
        aiGradingTaskMapper.updateById(task);

        if (BIZ_TYPE_PRACTICE.equals(task.getBizType())) {
            ExerciseRecord record = exerciseRecordMapper.selectById(task.getBizRecordId());
            if (record != null && !STATUS_SUCCESS.equals(record.getAiGradingStatus())) {
                record.setAiGradingStatus(STATUS_FAILED);
                record.setAiGradingErrorMessage(errorMessage);
                exerciseRecordMapper.updateById(record);
                PracticeSession session = record.getSessionRefId() == null ? null : practiceSessionMapper.selectById(record.getSessionRefId());
                updatePracticeSessionAfterSubjectiveResult(session, false, true);
            }
        } else if (BIZ_TYPE_EXAM.equals(task.getBizType())) {
            ExamRecordQuestion detail = examRecordQuestionMapper.selectById(task.getBizRecordId());
            if (detail != null && !STATUS_SUCCESS.equals(detail.getAiGradingStatus())) {
                detail.setAiGradingStatus(STATUS_FAILED);
                detail.setAiGradingErrorMessage(errorMessage);
                examRecordQuestionMapper.updateById(detail);
                recalculateExamRecordScore(detail.getExamRecordId());
            }
        }
    }

    private void persistRequestPayload(Long taskId, JSONObject auditPayload) {
        AiGradingTask task = aiGradingTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        task.setRequestPayloadJson(auditPayload.toJSONString());
        aiGradingTaskMapper.updateById(task);
    }

    private String buildTaskResponsePayload(GradingResult result) {
        JSONObject payload = new JSONObject();
        payload.put("parsed", parseStoredResponse(result.responsePayloadJson()));
        payload.put("rawResponse", result.rawResponse());
        return payload.toJSONString();
    }

    private Object parseStoredResponse(String responsePayloadJson) {
        if (!StringUtils.hasText(responsePayloadJson)) {
            return null;
        }
        try {
            return JSON.parseObject(responsePayloadJson);
        } catch (Exception exception) {
            return responsePayloadJson;
        }
    }

    private void updatePracticeSessionAfterSubjectiveResult(PracticeSession session, boolean isCorrect, boolean failed) {
        if (session == null) {
            return;
        }
        int pendingCount = Math.max(defaultNumber(session.getPendingSubjectiveCount()) - 1, 0);
        int failedCount = defaultNumber(session.getFailedSubjectiveCount()) + (failed ? 1 : 0);
        session.setPendingSubjectiveCount(pendingCount);
        session.setFailedSubjectiveCount(failedCount);
        if (!failed) {
            session.setCorrectCount(defaultNumber(session.getCorrectCount()) + (isCorrect ? 1 : 0));
            session.setWrongCount(defaultNumber(session.getWrongCount()) + (isCorrect ? 0 : 1));
        }
        session.setGradingStatus(resolveAggregateStatus(pendingCount, failedCount, true));
        practiceSessionMapper.updateById(session);
    }

    private void recalculateExamRecordScore(Long examRecordId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) {
            return;
        }
        List<ExamRecordQuestion> details = examRecordQuestionMapper.selectList(new LambdaQueryWrapper<ExamRecordQuestion>()
                .eq(ExamRecordQuestion::getExamRecordId, examRecordId));
        double objectiveScore = 0d;
        double subjectiveScore = 0d;
        int pendingCount = 0;
        int failedCount = 0;
        int correctCount = 0;
        boolean hasSubjective = false;

        for (ExamRecordQuestion detail : details) {
            String status = detail.getAiGradingStatus();
            if (Integer.valueOf(1).equals(detail.getIsCorrect())) {
                correctCount++;
            }
            if (STATUS_NOT_REQUIRED.equals(status)) {
                objectiveScore += defaultDouble(detail.getAwardedScore());
                continue;
            }
            if (StringUtils.hasText(status)) {
                hasSubjective = true;
            }
            if (STATUS_SUCCESS.equals(status)) {
                subjectiveScore += defaultDouble(detail.getAwardedScore());
            } else if (STATUS_FAILED.equals(status)) {
                failedCount++;
            } else if (STATUS_PENDING.equals(status) || STATUS_PROCESSING.equals(status)) {
                pendingCount++;
            }
        }

        record.setCorrectQuestions(correctCount);
        record.setObjectiveScore(roundTwoDecimals(objectiveScore));
        record.setSubjectiveScore(roundTwoDecimals(subjectiveScore));
        record.setScore(roundTwoDecimals(objectiveScore + subjectiveScore));
        record.setPendingSubjectiveCount(pendingCount);
        record.setFailedSubjectiveCount(failedCount);
        record.setGradingStatus(resolveAggregateStatus(pendingCount, failedCount, hasSubjective));
        examRecordMapper.updateById(record);
    }

    private String resolveAggregateStatus(int pendingCount, int failedCount, boolean hasSubjective) {
        if (!hasSubjective) {
            return STATUS_NOT_REQUIRED;
        }
        if (pendingCount > 0) {
            return STATUS_PROCESSING;
        }
        if (failedCount > 0) {
            return STATUS_PARTIAL_FAILED;
        }
        return STATUS_SUCCESS;
    }

    private ImageBundle loadImages(AnswerPayloadParser.ParsedAnswer parsedAnswer, Question question) {
        List<QwenGradingClient.ImageInput> images = new ArrayList<>();
        JSONArray auditImages = new JSONArray();
        Set<String> seenObjectNames = new LinkedHashSet<>();
        int maxImages = resolveMaxImages();

        addImageIfPossible(images, auditImages, seenObjectNames, "学生手写画布", parsedAnswer.canvasObjectName(), true, maxImages);
        for (String objectName : parseQuestionImageObjectNames(question.getImageUrls())) {
            addImageIfPossible(images, auditImages, seenObjectNames, "题目配图", objectName, false, maxImages);
        }
        for (AnswerPayloadParser.ImageAnnotation annotation : parsedAnswer.annotations()) {
            addImageIfPossible(images, auditImages, seenObjectNames, "题图原图", annotation.imageObjectName(), false, maxImages);
            addImageIfPossible(images, auditImages, seenObjectNames, "学生题图标注层", annotation.annotationObjectName(), true, maxImages);
        }
        return new ImageBundle(images, auditImages);
    }

    private void addImageIfPossible(
            List<QwenGradingClient.ImageInput> images,
            JSONArray auditImages,
            Set<String> seenObjectNames,
            String label,
            String objectName,
            boolean required,
            int maxImages
    ) {
        String normalized = normalizeText(objectName);
        if (!StringUtils.hasText(normalized) || isDirectUrl(normalized)) {
            return;
        }
        if (images.size() >= maxImages || !seenObjectNames.add(normalized)) {
            return;
        }
        try (InputStream stream = minioService.download(normalized)) {
            byte[] bytes = stream.readAllBytes();
            String mimeType = resolveMimeType(normalized);
            images.add(new QwenGradingClient.ImageInput(label, mimeType, Base64.getEncoder().encodeToString(bytes)));

            JSONObject auditImage = new JSONObject();
            auditImage.put("label", label);
            auditImage.put("objectName", normalized);
            auditImage.put("mimeType", mimeType);
            auditImage.put("bytes", bytes.length);
            auditImages.add(auditImage);
        } catch (Exception exception) {
            if (required) {
                throw new BusinessException("评分素材读取失败：" + normalized, 500);
            }
            log.debug("跳过不可读取的可选评分图片: {}", normalized);
        }
    }

    private List<String> parseQuestionImageObjectNames(String imageUrls) {
        if (!StringUtils.hasText(imageUrls)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String item : imageUrls.split(",")) {
            String normalized = normalizeText(item);
            if (StringUtils.hasText(normalized) && !isDirectUrl(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private JSONObject buildAuditPayload(
            AiGradingTask task,
            Question question,
            AnswerPayloadParser.ParsedAnswer parsedAnswer,
            JSONArray auditImages,
            Double fullScore
    ) {
        JSONObject payload = new JSONObject();
        payload.put("bizType", task.getBizType());
        payload.put("bizRecordId", task.getBizRecordId());
        payload.put("questionId", question.getId());
        payload.put("questionTitle", question.getTitle());
        payload.put("questionContent", question.getContent());
        payload.put("standardAnswer", question.getAnswer());
        payload.put("analysis", question.getAnalysis());
        payload.put("imageDesc", question.getImageDesc());
        payload.put("studentTextAnswer", parsedAnswer.text());
        payload.put("canvasObjectName", parsedAnswer.canvasObjectName());
        payload.put("imageAnnotations", JSON.toJSONString(parsedAnswer.annotations()));
        payload.put("images", auditImages);
        if (fullScore != null) {
            payload.put("fullScore", fullScore);
        }
        return payload;
    }

    private String buildSystemPrompt(GradingAttempt attempt) {
        if (BIZ_TYPE_PRACTICE.equals(attempt.task().getBizType())) {
            return """
                    你是高中数学练习题评分助手。请基于题目、标准答案、解析、学生文字作答、手写画布图片和题图标注进行判定。
                    练习模式只需要判定对错，不给分。判定规则：结论必须正确，关键步骤不能存在明显致命错误；文字和画布冲突时判错；证据不足或置信度过低时判错。
                    只返回 JSON：{"isCorrect":boolean,"confidence":0到1数字,"feedback":"给学生看的简短反馈","riskFlags":[],"reasoningSummary":"简短评分依据"}。
                    不要输出 Markdown，不要输出 JSON 以外的内容。
                    """;
        }
        return """
                你是高中数学考试主观题评分助手。请基于题目、标准答案、解析、学生文字作答、手写画布图片和题图标注进行评分。
                默认评分维度：结论正确 40%，关键步骤 40%，图示或过程有效性 20%。不要因为有图就加分，只认可图中有效解题信息。
                只返回 JSON：{"awardedScore":数字,"fullScore":数字,"confidence":0到1数字,"dimensionScores":{"conclusion":数字,"steps":数字,"diagram":数字},"feedback":"给学生看的简短反馈","riskFlags":[],"reasoningSummary":"简短评分依据","finalConclusion":"最终结论","usedEvidence":[]}。
                awardedScore 必须在 0 到 fullScore 之间。不要输出 Markdown，不要输出 JSON 以外的内容。
                """;
    }

    private String buildUserPrompt(GradingAttempt attempt) {
        Question question = attempt.question();
        StringBuilder prompt = new StringBuilder();
        prompt.append("题目标题：").append(nullToEmpty(question.getTitle())).append('\n');
        prompt.append("题目内容：").append(nullToEmpty(question.getContent())).append('\n');
        prompt.append("标准答案：").append(nullToEmpty(question.getAnswer())).append('\n');
        prompt.append("解析：").append(nullToEmpty(question.getAnalysis())).append('\n');
        prompt.append("图片说明：").append(nullToEmpty(question.getImageDesc())).append('\n');
        if (attempt.fullScore() != null) {
            prompt.append("本题满分：").append(attempt.fullScore()).append('\n');
        }
        prompt.append("学生文字作答：").append(nullToEmpty(attempt.parsedAnswer().text())).append('\n');
        if (StringUtils.hasText(attempt.parsedAnswer().canvasObjectName())) {
            prompt.append("学生提交了手写画布图片，图片随消息附带。\n");
        }
        if (!attempt.parsedAnswer().annotations().isEmpty()) {
            prompt.append("学生提交了题图标注，相关图片随消息附带。\n");
        }
        return prompt.toString();
    }

    private JSONObject parseJsonObject(String content) {
        String normalized = stripCodeFence(content);
        try {
            return JSON.parseObject(normalized);
        } catch (Exception exception) {
            throw new BusinessException("AI 评分结果不是合法 JSON", 502);
        }
    }

    private String stripCodeFence(String content) {
        String value = content == null ? "" : content.trim();
        if (value.startsWith("```")) {
            int firstLineEnd = value.indexOf('\n');
            int lastFence = value.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                return value.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return value;
    }

    private ExerciseRecord requireExerciseRecord(Long recordId) {
        ExerciseRecord record = exerciseRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("练习答题记录不存在", 404);
        }
        return record;
    }

    private ExamRecordQuestion requireExamRecordQuestion(Long recordQuestionId) {
        ExamRecordQuestion detail = examRecordQuestionMapper.selectById(recordQuestionId);
        if (detail == null) {
            throw new BusinessException("考试答题记录不存在", 404);
        }
        return detail;
    }

    private Question requireQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || Integer.valueOf(1).equals(question.getDeleted())) {
            throw new BusinessException("评分题目不存在或已删除", 404);
        }
        return question;
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
        if (tagIds.isEmpty()) {
            return new TagSnapshot(List.of(), List.of());
        }

        Map<Long, ExamTag> tagMap = examTagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(ExamTag::getId, Function.identity(), (left, right) -> left));
        List<String> tagNames = new ArrayList<>();
        for (Long tagId : tagIds) {
            ExamTag tag = tagMap.get(tagId);
            if (tag != null && StringUtils.hasText(tag.getName())) {
                tagNames.add(tag.getName());
            }
        }
        return new TagSnapshot(tagIds, tagNames);
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

    private boolean isDirectUrl(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.startsWith("data:")
                || lower.startsWith("blob:")
                || lower.startsWith("/");
    }

    private String resolveMimeType(String objectName) {
        String lower = objectName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/png";
    }

    private int resolveBatchSize() {
        Integer configured = qwenAiProperties.getGradingBatchSize();
        if (configured == null || configured <= 0) {
            return DEFAULT_BATCH_SIZE;
        }
        return Math.min(configured, 20);
    }

    private int resolveMaxImages() {
        Integer configured = qwenAiProperties.getGradingMaxImages();
        if (configured == null || configured <= 0) {
            return DEFAULT_MAX_IMAGES;
        }
        return Math.min(configured, 12);
    }

    private Double clampConfidence(Double value) {
        if (value == null || value.isNaN()) {
            return null;
        }
        return Math.min(Math.max(value, 0d), 1d);
    }

    private double clampScore(Double value, double fullScore) {
        if (value == null || value.isNaN()) {
            return 0d;
        }
        return roundTwoDecimals(Math.min(Math.max(value, 0d), Math.max(fullScore, 0d)));
    }

    private double roundTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0d : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String truncateError(String message) {
        String normalized = StringUtils.hasText(message) ? message.trim() : "AI 评分任务执行失败";
        if (normalized.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }

    private record ImageBundle(List<QwenGradingClient.ImageInput> images, JSONArray auditImages) {
    }

    private record TagSnapshot(List<Long> tagIds, List<String> tagNames) {
    }

    private record GradingAttempt(
            AiGradingTask task,
            ExerciseRecord practiceRecord,
            ExamRecordQuestion examRecordQuestion,
            Question question,
            AnswerPayloadParser.ParsedAnswer parsedAnswer,
            List<QwenGradingClient.ImageInput> images,
            JSONObject auditPayload,
            Double fullScore
    ) {
    }

    private record GradingResult(
            AiGradingTask task,
            boolean isCorrect,
            Double awardedScore,
            Double confidence,
            String feedback,
            String responsePayloadJson,
            String rawResponse
    ) {
    }
}
