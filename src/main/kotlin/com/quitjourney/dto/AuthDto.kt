package com.quitjourney.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * 用户注册请求DTO
 */
data class RegisterRequest(
    @field:Email(message = "邮箱格式不正确")
    @field:NotBlank(message = "邮箱不能为空")
    val email: String,
    
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    val password: String,
    
    @field:Size(max = 100, message = "姓名长度不能超过100个字符")
    val name: String? = null,
    
    val agreeToTerms: Boolean = false
)

/**
 * 用户登录请求DTO
 */
data class LoginRequest(
    @field:Email(message = "邮箱格式不正确")
    @field:NotBlank(message = "邮箱不能为空")
    val email: String,
    
    @field:NotBlank(message = "密码不能为空")
    val password: String,
    
    val deviceInfo: DeviceInfo? = null
)

/**
 * 设备信息DTO
 */
data class DeviceInfo(
    val deviceId: String,
    val platform: String,
    val version: String
)

/**
 * 刷新令牌请求DTO
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "刷新令牌不能为空")
    val refreshToken: String
)

/**
 * 认证响应DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthResponse(
    val user: UserDto,
    val tokens: TokenInfo
)

/**
 * 令牌信息DTO
 */
data class TokenInfo(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)

/**
 * 用户基本信息DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    val id: String,
    val email: String,
    val name: String? = null,
    val emailVerified: Boolean,
    val profile: UserProfileDto? = null,
    val createdAt: Instant
)

/**
 * 用户资料DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserProfileDto(
    val quitDate: Instant? = null,
    val quitReason: String? = null,
    val cigarettesPerDay: Int? = null,
    val cigarettePrice: Double? = null,
    val currency: String = "CNY",
    val timezone: String = "UTC",
    val locale: String = "zh-CN"
)

/**
 * 忘记密码请求DTO
 */
data class ForgotPasswordRequest(
    @field:Email(message = "邮箱格式不正确")
    @field:NotBlank(message = "邮箱不能为空")
    val email: String
)

/**
 * 重置密码请求DTO
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "重置令牌不能为空")
    val token: String,
    
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    val newPassword: String
)
