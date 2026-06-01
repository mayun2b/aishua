package zysy.iflytek.aishua.common.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    private String message;
    private int code;

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }

    /**
     * 构造方法，注入当前类所需依赖。
     */
    public BusinessException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    /**
     * 查询并返回处理结果。
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 查询并返回处理结果。
     */
    public int getCode() {
        return code;
    }
}