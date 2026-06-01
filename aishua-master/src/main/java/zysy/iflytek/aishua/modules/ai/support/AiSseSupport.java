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
     * 构造方法，注入当前类所需依赖。
     */
    private AiSseSupport() {
    }

    /**
     * 发送单条 SSE 事件，统一做 JSON 序列化。
     */
    public static void emitEvent(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(JSON.toJSONString(payload)));
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    /**
     * 统一处理流式异常并向前端回传错误事件。
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
     * 解包运行时异常，尽量拿到根因异常类型。
     */
    private static Throwable unwrapCause(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException && runtimeException.getCause() != null) {
            return runtimeException.getCause();
        }
        return throwable;
    }
}
