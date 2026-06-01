package zysy.iflytek.aishua.common.result;

import lombok.Data;

/**
 * 通用基础统一返回结果对象，封装接口响应码、消息与数据体。
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    /**
     * 处理当前业务逻辑。
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 处理当前业务逻辑。
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 处理当前业务逻辑。
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 处理当前业务逻辑。
     */
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    /**
     * 处理当前业务逻辑。
     */
    public static <T> Result<T> unauth(String message) {
        return fail(401, message);
    }
}
