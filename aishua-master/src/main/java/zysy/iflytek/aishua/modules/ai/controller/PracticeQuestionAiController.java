package zysy.iflytek.aishua.modules.ai.controller;

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
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.PracticeQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.PracticeQuestionAiChatSessionVO;
import zysy.iflytek.aishua.modules.ai.service.PracticeQuestionAiService;
import zysy.iflytek.aishua.modules.ai.support.AiSseSupport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 单题智能助教接口。
 */
@RestController
@RequestMapping("/api/practice/{sessionId}/questions/{questionId}/assistant")
public class PracticeQuestionAiController {
    private final PracticeQuestionAiService practiceQuestionAiService;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public PracticeQuestionAiController(PracticeQuestionAiService practiceQuestionAiService) {
        this.practiceQuestionAiService = practiceQuestionAiService;
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/sessions/latest")
    public Result<PracticeQuestionAiChatSessionVO> getLatestSession(
            @PathVariable Long sessionId,
            @PathVariable Long questionId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceQuestionAiService.getLatestChatSession(userId, sessionId, questionId));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping("/sessions")
    public Result<PracticeQuestionAiChatSessionVO> createSession(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody(required = false) PracticeQuestionAiCreateSessionDTO requestDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        PracticeQuestionAiCreateSessionDTO payload = requestDTO == null ? new PracticeQuestionAiCreateSessionDTO() : requestDTO;
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceQuestionAiService.createChatSession(userId, sessionId, questionId, payload));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/sessions/{assistantSessionId}/messages")
    public Result<List<PracticeQuestionAiChatMessageVO>> listMessages(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceQuestionAiService.listChatMessages(userId, sessionId, questionId, assistantSessionId));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping("/sessions/{assistantSessionId}/messages")
    public Result<PracticeQuestionAiChatMessageVO> sendMessage(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId,
            @Valid @RequestBody PracticeQuestionAiSendMessageDTO requestDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceQuestionAiService.sendChatMessage(
                userId,
                sessionId,
                questionId,
                assistantSessionId,
                requestDTO
        ));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping(value = "/sessions/{assistantSessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId,
            @Valid @RequestBody PracticeQuestionAiSendMessageDTO requestDTO
    ) {
        // 向前端持续推送增量回答片段。
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        SseEmitter emitter = new SseEmitter(AiSseSupport.DEFAULT_STREAM_TIMEOUT_MS);
        CompletableFuture.runAsync(() -> {
            try {
                PracticeQuestionAiChatMessageVO message = practiceQuestionAiService.streamChatMessage(
                        userId,
                        sessionId,
                        questionId,
                        assistantSessionId,
                        requestDTO,
                        delta -> AiSseSupport.emitEvent(emitter, "chunk", Map.of("delta", delta))
                );
                AiSseSupport.emitEvent(emitter, "done", message);
                emitter.complete();
            } catch (Exception exception) {
                AiSseSupport.emitError(emitter, exception, "AI service error, please retry later");
            }
        });
        return emitter;
    }

    /**
     * 删除接口入口，负责资源删除与结果返回。
     */
    @PutMapping("/sessions/{assistantSessionId}/close")
    public Result<PracticeQuestionAiChatSessionVO> closeSession(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @PathVariable Long assistantSessionId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(practiceQuestionAiService.closeChatSession(
                userId,
                sessionId,
                questionId,
                assistantSessionId
        ));
    }
}
