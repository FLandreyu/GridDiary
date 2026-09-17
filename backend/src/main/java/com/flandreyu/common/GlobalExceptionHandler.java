package com.flandreyu.common;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理：统一返回 Result 结构，避免把堆栈抛给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：HTTP 状态码跟随 code（400/401/403/404...） */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        int httpStatus = (e.getCode() >= 400 && e.getCode() < 600) ? e.getCode() : 400;
        return ResponseEntity.status(httpStatus).body(Result.error(e.getCode(), e.getMessage()));
    }

    /** @Valid 参数校验失败：取第一条字段错误信息 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(Result.error(400, message));
    }

    /** 上传文件超出大小限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(Result.error(400, "上传文件过大，单张不超过 5MB"));
    }

    /**
     * 静态资源不存在（图片被删/路径失效，Spring 6.1+ 会抛 NoResourceFoundException）。
     *
     * <p>
     * 必须单独处理：否则会落到下面的兜底分支，把「图片不存在」当成 500 服务器错误，
     * 既误导前端、又会刷满堆栈日志。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("静态资源不存在：{}", e.getMessage());
        return ResponseEntity.status(404).body(Result.error(404, "资源不存在"));
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return ResponseEntity.status(500).body(Result.error(500, "服务器内部错误"));
    }
}
