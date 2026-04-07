package zysy.iflytek.aishuai.common.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    private String message;
    private int code;

    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }

    public BusinessException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
