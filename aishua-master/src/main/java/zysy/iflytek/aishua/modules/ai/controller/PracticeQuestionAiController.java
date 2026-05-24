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
import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatSessionVO;
import zysy.iflytek.aishua.modules.ai.service.PracticeQuestionAiService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/practice/{sessionId}/questions/{questionId}/assistant")
public class PracticeQuestionAiController {
    private static final long STREAM_TIMEOUT_MS = 120_000L;

    private final PracticeQuestionAiService practiceQuestionAiService;

    public PracticeQuestionAiController(PracticeQuestionAiService practiceQuestionAiService) {
        this.practiceQuestionAiService = practiceQuestionAiService;
    }

    @GetMapping("/sessions/latest")
    public Result<PracticeQuestionAiChatSessionVO> getLatestSession(
            @PathVariable Long sessionId,
            @PathVariable Long questionId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceQuestionAiService.getLatestChatSession(userId, sessionId, questionId));
    }

    @PostMapping("/sessions")
    public Result<PracticeQuestionAiChatSessionVO> createSession(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody(required = false) PracticeQuestionAiCreateSessionDTO requestDTO
    ) {
        Long userId = UserContext.requireUserId();
        PracticeQuestionAiCreateSessionDTO payload = requestDTO == null ? new PracticeQuestionAiCreateSessionDTO() : requestDTO;
        return Result.success(practiceQuestionAiService.createChatSession(userId, sessionId, questionId, payload));
    }

    @GetMapping("/sessions/{assistantSessionId}/messages")
    public Result<List<PracticeQuestionAiChatMessageVO>> listMessages(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceQuestionAiService.listChatMessages(userId, sessionId, questionId, assistantSessionId));
    }

    @PostMapping("/sessions/{assistantSessionId}/messages")
    public Result<PracticeQuestionAiChatMessageVO> sendMessage(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId,
            @Valid @RequestBody PracticeQuestionAiSendMessageDTO requestDTO
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceQuestionAiService.sendChatMessage(
                userId,
                sessionId,
                questionId,
                assistantSessionId,
                requestDTO
        ));
    }

    @PostMapping(value = "/sessions/{assistantSessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId,
            @Valid @RequestBody PracticeQuestionAiSendMessageDTO requestDTO
    ) {
        Long userId = UserContext.requireUserId();
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        CompletableFuture.runAsync(() -> {
            try {
                PracticeQuestionAiChatMessageVO message = practiceQuestionAiService.streamChatMessage(
                        userId,
                        sessionId,
                        questionId,
                        assistantSessionId,
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

    @PutMapping("/sessions/{assistantSessionId}/close")
    public Result<PracticeQuestionAiChatSessionVO> closeSession(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId
    ) {
        Long userId = UserContext.requireUserId();
        return Result.success(practiceQuestionAiService.closeChatSession(
                userId,
                sessionId,
                questionId,
                assistantSessionId
        ));
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
