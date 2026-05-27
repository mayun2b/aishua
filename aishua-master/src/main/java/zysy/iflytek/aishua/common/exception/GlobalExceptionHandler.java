package zysy.iflytek.aishua.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import zysy.iflytek.aishua.common.result.Result;

/**
 * 将后端异常统一映射为稳定的接口响应结构。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String INVALID_REQUEST_MESSAGE = "请求参数不合法";

    /**
     * 处理当前业务逻辑。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理当前业务逻辑。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return Result.fail(400, firstFieldMessage(extractFieldMessage(exception.getBindingResult())));
    }

    /**
     * 处理当前业务逻辑。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException exception) {
        return Result.fail(400, firstFieldMessage(extractFieldMessage(exception.getBindingResult())));
    }

    /**
     * 处理当前业务逻辑。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        return Result.fail(400, INVALID_REQUEST_MESSAGE);
    }

    /**
     * 处理当前业务逻辑。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return Result.fail(400, "请求体格式不合法");
    }

    /**
     * 处理当前业务逻辑。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("未处理异常", exception);
        return Result.fail(500, "服务器内部错误");
    }

    /**
     * 处理当前业务逻辑。
     */
    private String firstFieldMessage(String fieldMessage) {
        return fieldMessage == null || fieldMessage.isBlank() ? INVALID_REQUEST_MESSAGE : fieldMessage;
    }

    /**
     * 解析并转换输入数据。
     */
    private String extractFieldMessage(BindingResult bindingResult) {
        return bindingResult.getFieldError() == null ? null : bindingResult.getFieldError().getDefaultMessage();
    }
}
