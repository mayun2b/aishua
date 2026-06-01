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
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiAnalysisRequestDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiCreateSessionDTO;
import zysy.iflytek.aishua.modules.ai.entity.dto.WrongQuestionAiSendMessageDTO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiAnalysisVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatMessageVO;
import zysy.iflytek.aishua.modules.ai.entity.vo.WrongQuestionAiChatSessionVO;
import zysy.iflytek.aishua.modules.ai.service.WrongQuestionAiService;
import zysy.iflytek.aishua.modules.ai.support.AiSseSupport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 错题智能分析与对话接口。
 */
@RestController
@RequestMapping("/api/practice/wrong-questions")
public class WrongQuestionAiController {
    private final WrongQuestionAiService wrongQuestionAiService;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public WrongQuestionAiController(WrongQuestionAiService wrongQuestionAiService) {
        this.wrongQuestionAiService = wrongQuestionAiService;
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping("/{wrongQuestionId}/ai-analysis")
    public Result<WrongQuestionAiAnalysisVO> analyzeWrongQuestion(
            @PathVariable Long wrongQuestionId,
            @Valid @RequestBody(required = false) WrongQuestionAiAnalysisRequestDTO requestDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        WrongQuestionAiAnalysisRequestDTO payload = requestDTO == null ? new WrongQuestionAiAnalysisRequestDTO() : requestDTO;
        // 调用服务层处理业务并封装统一响应。
        return Result.success(wrongQuestionAiService.analyzeWrongQuestion(userId, wrongQuestionId, payload));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/{wrongQuestionId}/ai-analysis/latest")
    public Result<WrongQuestionAiAnalysisVO> getLatestAnalysis(@PathVariable Long wrongQuestionId) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(wrongQuestionAiService.getLatestAnalysis(userId, wrongQuestionId));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/{wrongQuestionId}/ai-chat/sessions/latest")
    public Result<WrongQuestionAiChatSessionVO> getLatestSession(@PathVariable Long wrongQuestionId) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(wrongQuestionAiService.getLatestChatSession(userId, wrongQuestionId));
    }

    /**
     * 处理创建请求并返回结果。
     */
    @PostMapping("/{wrongQuestionId}/ai-chat/sessions")
    public Result<WrongQuestionAiChatSessionVO> createSession(
            @PathVariable Long wrongQuestionId,
            @RequestBody(required = false) WrongQuestionAiCreateSessionDTO requestDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        WrongQuestionAiCreateSessionDTO payload = requestDTO == null ? new WrongQuestionAiCreateSessionDTO() : requestDTO;
        // 调用服务层处理业务并封装统一响应。
        return Result.success(wrongQuestionAiService.createChatSession(userId, wrongQuestionId, payload));
    }

    /**
     * 处理查询请求并返回结果。
     */
    @GetMapping("/{wrongQuestionId}/ai-chat/sessions/{sessionId}/messages")
    public Result<List<WrongQuestionAiChatMessageVO>> listMessages(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(wrongQuestionAiService.listChatMessages(userId, wrongQuestionId, sessionId));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping("/{wrongQuestionId}/ai-chat/sessions/{sessionId}/messages")
    public Result<WrongQuestionAiChatMessageVO> sendMessage(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId,
            @Valid @RequestBody WrongQuestionAiSendMessageDTO requestDTO
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(wrongQuestionAiService.sendChatMessage(userId, wrongQuestionId, sessionId, requestDTO));
    }

    /**
     * 接口处理入口，负责参数接收与服务调用。
     */
    @PostMapping(value = "/{wrongQuestionId}/ai-chat/sessions/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId,
            @Valid @RequestBody WrongQuestionAiSendMessageDTO requestDTO
    ) {
        // 向前端持续推送增量回答片段。
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        SseEmitter emitter = new SseEmitter(AiSseSupport.DEFAULT_STREAM_TIMEOUT_MS);
        CompletableFuture.runAsync(() -> {
            try {
                WrongQuestionAiChatMessageVO message = wrongQuestionAiService.streamChatMessage(
                        userId,
                        wrongQuestionId,
                        sessionId,
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
    @PutMapping("/{wrongQuestionId}/ai-chat/sessions/{sessionId}/close")
    public Result<WrongQuestionAiChatSessionVO> closeSession(
            @PathVariable Long wrongQuestionId,
            @PathVariable Long sessionId
    ) {
        // 从用户上下文获取当前登录用户编号。
        Long userId = UserContext.requireUserId();
        // 调用服务层处理业务并封装统一响应。
        return Result.success(wrongQuestionAiService.closeChatSession(userId, wrongQuestionId, sessionId));
    }
}
