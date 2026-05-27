package zysy.iflytek.aishua.modules.ai.support;

import com.alibaba.fastjson2.JSON;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import zysy.iflytek.aishua.common.exception.BusinessException;

import java.io.IOException;
import java.util.Map;

/**
 * 智能问答控制器复用的流式响应工具。
 */
public final class AiSseSupport {
    public static final long DEFAULT_STREAM_TIMEOUT_MS = 120_000L;

    /**
     * 构造方法，负责注入依赖组件。
     */
    private AiSseSupport() {
    }

    /**
     * 提供通用支撑处理能力。
     */
    public static void emitEvent(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(JSON.toJSONString(payload)));
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    /**
     * 提供通用支撑处理能力。
     */
    public static void emitError(SseEmitter emitter, Exception exception, String defaultMessage) {
        Throwable rootCause = unwrapCause(exception);
        if (rootCause instanceof IOException) {
            emitter.complete();
            return;
        }

        String message = rootCause instanceof BusinessException
                ? rootCause.getMessage()
                : defaultMessage;
        try {
            emitEvent(emitter, "error", Map.of("message", message));
            emitter.complete();
        } catch (RuntimeException runtimeException) {
            emitter.completeWithError(rootCause);
        }
    }

    /**
     * 提供通用支撑处理能力。
     */
    private static Throwable unwrapCause(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException && runtimeException.getCause() != null) {
            return runtimeException.getCause();
        }
        return throwable;
    }
}
