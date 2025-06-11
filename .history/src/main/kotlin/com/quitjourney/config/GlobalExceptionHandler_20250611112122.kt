package com.quitjourney.config

import com.quitjourney.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.concurrent.ConcurrentHashMap

/**
 * 全局异常处理器
 * 
 * 统一处理验证错误和其他异常，确保正确的HTTP状态码和错误信息返回
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * 处理验证错误
     * 
     * 当@Valid注解的参数验证失败时，Spring会抛出MethodArgumentNotValidException
     * 我们需要正确处理这些错误，返回400状态码而不是被Spring Security转换为401
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val fieldErrors = mutableListOf<com.quitjourney.dto.FieldError>()
        
        // 收集所有字段验证错误
        ex.bindingResult.fieldErrors.forEach { error ->
            fieldErrors.add(
                com.quitjourney.dto.FieldError(
                    field = error.field,
                    code = error.code ?: "validation_error",
                    message = error.defaultMessage ?: "验证失败"
                )
            )
        }
        
        // 获取第一个错误作为主要错误信息
        val primaryError = fieldErrors.firstOrNull()?.message ?: "输入数据验证失败"
        
        println("验证错误详情: ${fieldErrors.map { "${it.field}: ${it.message}" }}")
        println("返回错误信息: $primaryError")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", primaryError, fieldErrors))
    }
    
    /**
     * 处理一般的非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        println("业务逻辑错误: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("BUSINESS_ERROR", ex.message ?: "请求参数错误"))
    }
    
    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        println("未处理异常: ${ex.javaClass.simpleName}: ${ex.message}")
        ex.printStackTrace()
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
    }
} 