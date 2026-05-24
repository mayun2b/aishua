package zysy.iflytek.aishua.modules.ai.controller;

import com.alibaba.fastjson2.JSON;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.exception.BusinessException;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiAnalysisRequestDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiAnalysisVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatSessionVO;
import zysy.iflytek.aishua.modules.ai.service.WrongQuestionAiService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/practice/wrong-questions")
public class WrongQuestionAiController {
    private static final long STREAM_TIMEOUT_MS = 120_000L;

    private final WrongQuestionAiService wrongQuestionAiService;

    public WrongQuestionAiController(WrongQuestionAiService wrongQuestionAiService) {
        this.wrongQuestionAiService = wrongQuestionAiService;
    }

    @PostMapping("/{wrongQuestionId}/ai-analysis")
    public Result<WrongQuestionAiAnalysisVO> analyzeWrongQuestion(
            @PathVariable Long wrongQuestionId,
            @Valid @RequestBody(required = false) WrongQuestionAiAnalysisRequestDTO requestDTO
    ) {
        Long userId = UserContext.requireUserId();
        WrongQuestionAiAnalysisRequestDTO payload = requestDTO == null ? new WrongQuestionAiAnalysisRequestDTO() : requestDTO;
        return Result.success(wrongQuestionAiService.analyzeWrongQuestion(userId, wrongQuestionId, payload));
    }

    @GetMapping("/{wrongQuestionId}/ai-analysis/latest")
    public Result<WrongQuestionAiAnalysisVO> getLatestAnalysis(@PathVariable Long wrongQuestionId) {
        Long userId = UserContext.requireUserId();
        return Result.success(wrongQuestionAiService.getLatestAnalysis(userId, wrongQuestionId));
    }

    @GetMapping("/{wrongQuestionId}/ai-chat/sessions/latest")
    public Result<WrongQuestionAiChatSessionVO> getLatestSession(@PathVariable Long wrongQuestionId) {
        Long userId = UserContext.requireUserId();
        return Result.success(wrongQuestionAiService.getLatestChatSession(userId, wrongQuestionId));
    }

    @PostMapping("/{wrongQuestionId}/ai-chat/sessions")
    public Result<WrongQuestionAiChatSessionVO> createSession(
            @PathVariable Long wrongQuestionId,
            @RequestBody(required = false) WrongQuestionAiCreateSessionDTO requestDTO
    ) {
        Long userId = UserContext.requireUserId();
        WrongQuestionAiCreateSessionDTO payload = requestDTO == null ? new WrongQuestionAiCreateSessionDTO() : requestDTO;
        return Result.success(wrongQuestionAiService.createChatSession(userId, wrongQuestionId, payload));
    }

    @GetMapping("/{wrongQuestionId}/ai-chat/sessions/{sessionId}/messages")
    public Result<List<WrongQuestionAiChatMessageVO>> listMessages(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(wrongQuestionAiService.listChatMessages(userId, wrongQuestionId, sessionId));
    }

    @PostMapping("/{wrongQuestionId}/ai-chat/sessions/{sessionId}/messages")
    public Result<WrongQuestionAiChatMessageVO> sendMessage(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId,
            @Valid @RequestBody WrongQuestionAiSendMessageDTO requestDTO
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(wrongQuestionAiService.sendChatMessage(userId, wrongQuestionId, sessionId, requestDTO));
    }

    @PostMapping(value = "/{wrongQuestionId}/ai-chat/sessions/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId,
            @Valid @RequestBody WrongQuestionAiSendMessageDTO requestDTO
    ) {
        Long userId = UserContext.requireUserId();
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        CompletableFuture.runAsync(() -> {
            try {
                WrongQuestionAiChatMessageVO message = wrongQuestionAiService.streamChatMessage(
                        userId,
                        wrongQuestionId,
                        sessionId,
                        requestDTO,
                        delta -> emitEvent(emitter, "chunk", Map.of("delta", delta))
                );
                emitEvent(emitter, "done", message);
                emitter.complete();
            } catch (Exception exception) {
                emitError(emitter, exception);
            }
        });
        return emitter;
    }

    @PutMapping("/{wrongQuestionId}/ai-chat/sessions/{sessionId}/close")
    public Result<WrongQuestionAiChatSessionVO> closeSession(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(wrongQuestionAiService.closeChatSession(userId, wrongQuestionId, sessionId));
    }

    private void emitEvent(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(JSON.toJSONString(payload)));
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    private void emitError(SseEmitter emitter, Exception exception) {
        Throwable rootCause = unwrapCause(exception);
        if (rootCause instanceof IOException) {
            emitter.complete();
            return;
        }

        String message = rootCause instanceof BusinessException
                ? rootCause.getMessage()
                : "AI 服务异常，请稍后重试";
        try {
            emitter.send(SseEmitter.event().name("error").data(JSON.toJSONString(Map.of("message", message))));
            emitter.complete();
        } catch (IOException ioException) {
            emitter.completeWithError(rootCause);
        }
    }

    private Throwable unwrapCause(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException && runtimeException.getCause() != null) {
            return runtimeException.getCause();
        }
        return throwable;
    }
}
