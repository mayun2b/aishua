package zysy.iflytek.aishuai.common.result;

import lombok.Data;

/**
 * 全局统一返回结果
 */
@Data
public class Result<T> {
    // 响应码：200成功，401未登录，500失败
    private Integer code;
    // 响应消息
    private String message;
    // 响应数据
    private T data;

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 成功（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    // 未登录/权限不足
    public static <T> Result<T> unauth(String message) {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
}