package com.quitjourney.controller

import com.quitjourney.dto.*
import com.quitjourney.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 认证控制器
 * 
 * 处理用户注册、登录、令牌刷新等认证相关请求
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户认证相关API")
class AuthController(
    private val authService: AuthService
) {
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val authResponse = authService.register(request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponse))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error("REGISTRATION_ERROR", e.message ?: "注册失败"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户身份验证并获取访问令牌")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val authResponse = authService.login(request)
            ResponseEntity.ok(ApiResponse.success(authResponse))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("LOGIN_ERROR", e.message ?: "登录失败"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<TokenInfo>> {
        return try {
            val tokenInfo = authService.refreshToken(request)
            ResponseEntity.ok(ApiResponse.success(tokenInfo))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("TOKEN_ERROR", e.message ?: "令牌刷新失败"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "注销用户会话")
    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<ApiResponse<String>> {
        return try {
            // 从Authorization头中提取用户ID（这里简化处理）
            val success = authService.logout("user-id")
            if (success) {
                ResponseEntity.ok(ApiResponse.success("登出成功"))
            } else {
                ResponseEntity.badRequest()
                    .body(ApiResponse.error("LOGOUT_ERROR", "登出失败"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            val success = authService.forgotPassword(request)
            if (success) {
                ResponseEntity.ok(ApiResponse.success("密码重置邮件已发送"))
            } else {
                ResponseEntity.badRequest()
                    .body(ApiResponse.error("EMAIL_ERROR", "邮件发送失败"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "使用重置令牌更新密码")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            val success = authService.resetPassword(request)
            if (success) {
                ResponseEntity.ok(ApiResponse.success("密码重置成功"))
            } else {
                ResponseEntity.badRequest()
                    .body(ApiResponse.error("RESET_ERROR", "密码重置失败"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 邮箱验证
     */
    @GetMapping("/verify-email")
    @Operation(summary = "邮箱验证", description = "验证用户邮箱地址")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<ApiResponse<String>> {
        return try {
            val success = authService.verifyEmail(token)
            if (success) {
                ResponseEntity.ok(ApiResponse.success("邮箱验证成功"))
            } else {
                ResponseEntity.badRequest()
                    .body(ApiResponse.error("VERIFICATION_ERROR", "邮箱验证失败"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
}
