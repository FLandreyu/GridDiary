package com.flandreyu.common;

import lombok.Data;

/**
 * 统一响应体：{ code, message, data }
 * code 约定：200 成功 / 400 参数或业务错误 / 401 未登录 / 500 服务器错误
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
