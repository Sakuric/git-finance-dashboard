package com.example.financedashboard.exception;

import com.example.financedashboard.utils.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 使用@RestControllerAdvice注解，可以全局处理Controller层抛出的异常
 * 避免在每个Controller方法中都添加try-catch块，使代码更加简洁
 */
@RestControllerAdvice  // 结合@ControllerAdvice和@ResponseBody，表示全局异常处理器并返回JSON数据
public class GlobalExceptionHandler {

    /**
     * 处理RuntimeException异常
     * @param e 捕获的运行时异常
     * @return 封装后的错误Result对象
     */
    @ExceptionHandler(RuntimeException.class)  // 指定处理的异常类型
    public Result<String> handleRuntimeException(RuntimeException e) {
        // 返回状态码500和异常消息
        return Result.error(500, e.getMessage());
    }
    
    /**
     * 处理AccessDeniedException异常（403 Forbidden）
     * @param e 捕获的访问拒绝异常
     * @return 封装后的错误Result对象
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        // 返回状态码403和异常消息
        return Result.error(403, "访问被拒绝: " + e.getMessage());
    }
    
    /**
     * 处理JWT认证异常
     * @param e 捕获的JWT认证异常
     * @return 封装后的错误Result对象
     */
    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class, AuthenticationCredentialsNotFoundException.class})
    public Result<String> handleJwtException(Exception e) {
        // 返回状态码401和异常消息
        return Result.error(401, "Token无效或已过期: " + e.getMessage());
    }
}