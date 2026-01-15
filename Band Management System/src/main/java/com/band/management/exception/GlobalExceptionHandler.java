package com.band.management.exception;

import com.band.management.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: URI={}, Code={}, Message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        
        // 对于未授权错误，返回401状态码（但保持响应体中的code为2000）
        if (e.getCode().equals(ErrorCode.UNAUTHORIZED.getCode())) {
            log.warn("未授权访问: URI={}", request.getRequestURI());
        }
        
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e, HttpServletRequest request) {
        log.error("参数校验异常: URI={}", request.getRequestURI());
        
        String message;
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        } else {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        }
        
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 认证失败异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.error("认证失败: URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ErrorCode.LOGIN_FAILED.getCode(), ErrorCode.LOGIN_FAILED.getMessage());
    }

    /**
     * 权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("权限不足: URI={}, Message={}", request.getRequestURI(), e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    /**
     * SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public Result<?> handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("数据库异常: URI={}, SQLState={}, ErrorCode={}, Message={}", 
                request.getRequestURI(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
        
        // 提取触发器或约束的错误信息
        String message = e.getMessage();
        
        // MySQL触发器错误的SQLState通常是45000
        if ("45000".equals(e.getSQLState()) && message != null) {
            // 提取触发器中SIGNAL抛出的MESSAGE_TEXT
            // 格式通常是: ... (conn=xxx) xxx
            int lastParenIndex = message.lastIndexOf(')');
            if (lastParenIndex > 0 && lastParenIndex < message.length() - 1) {
                String extractedMessage = message.substring(lastParenIndex + 1).trim();
                if (!extractedMessage.isEmpty()) {
                    return Result.error(ErrorCode.PARAM_ERROR.getCode(), extractedMessage);
                }
            }
            // 如果无法提取，返回完整消息
            return Result.error(ErrorCode.PARAM_ERROR.getCode(), message);
        }
        
        // 其他SQL异常返回通用错误
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "数据库操作失败");
    }

    /**
     * Spring JDBC包装的SQL异常
     */
    @ExceptionHandler(UncategorizedSQLException.class)
    public Result<?> handleUncategorizedSQLException(UncategorizedSQLException e, HttpServletRequest request) {
        log.error("数据库异常(UncategorizedSQLException): URI={}, Message={}", 
                request.getRequestURI(), e.getMessage(), e);
        
        // 获取原始的SQLException
        SQLException sqlException = e.getSQLException();
        if (sqlException != null) {
            return handleSQLException(sqlException, request);
        }
        
        // 如果无法获取原始SQLException，尝试从消息中提取
        String message = e.getMessage();
        if (message != null) {
            // UncategorizedSQLException的消息格式通常包含原始错误信息
            // 尝试提取最后一行（通常是实际的错误消息）
            String[] lines = message.split("\n");
            for (int i = lines.length - 1; i >= 0; i--) {
                String line = lines[i].trim();
                // 跳过空行和以"###"开头的行
                if (!line.isEmpty() && !line.startsWith("###") && !line.startsWith("nested exception")) {
                    // 移除可能的前缀（如"Cause: java.sql.SQLException: "）
                    int colonIndex = line.lastIndexOf(": ");
                    if (colonIndex > 0 && colonIndex < line.length() - 1) {
                        String extractedMessage = line.substring(colonIndex + 2).trim();
                        if (!extractedMessage.isEmpty()) {
                            return Result.error(ErrorCode.PARAM_ERROR.getCode(), extractedMessage);
                        }
                    }
                    // 如果没有冒号，直接返回这一行
                    return Result.error(ErrorCode.PARAM_ERROR.getCode(), line);
                }
            }
        }
        
        // 如果无法提取，返回通用错误
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "数据库操作失败");
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: URI={}", request.getRequestURI(), e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统内部错误");
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: URI={}, Message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误，请联系管理员");
    }
}
