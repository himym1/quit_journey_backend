package com.quitjourney.controller

import com.quitjourney.dto.*
import com.quitjourney.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户资料和设置相关API")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService
) {
    
    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户资料", description = "获取当前用户的详细资料信息")
    fun getUserProfile(authentication: Authentication): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val userDto = userService.getUserProfile(userId)
            
            if (userDto != null) {
                ResponseEntity.ok(ApiResponse.success(userDto))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(500)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户资料", description = "更新当前用户的资料信息")
    fun updateUserProfile(
        @Valid @RequestBody profileDto: UserProfileDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val updatedUser = userService.updateUserProfile(userId, profileDto)
            ResponseEntity.ok(ApiResponse.success(updatedUser))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.message ?: "参数验证失败"))
        } catch (e: Exception) {
            ResponseEntity.status(500)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 删除用户账户
     */
    @DeleteMapping("/account")
    @Operation(summary = "删除用户账户", description = "永久删除用户账户及所有相关数据")
    fun deleteUserAccount(authentication: Authentication): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val success = userService.deleteUserAccount(userId)
            
            if (success) {
                ResponseEntity.ok(ApiResponse.success("账户删除成功"))
            } else {
                ResponseEntity.status(500)
                    .body(ApiResponse.error("DELETE_ERROR", "账户删除失败"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户统计", description = "获取用户的戒烟统计数据")
    fun getUserStats(authentication: Authentication): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val stats = userService.getUserStats(userId)
            ResponseEntity.ok(ApiResponse.success(stats))
        } catch (e: Exception) {
            ResponseEntity.status(500)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 获取用户设置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取用户设置", description = "获取用户的应用设置")
    fun getUserSettings(authentication: Authentication): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            // TODO: 实现用户设置获取逻辑
            val settings = mapOf(
                "notifications" to mapOf(
                    "dailyReminder" to true,
                    "achievementNotification" to true,
                    "weeklyReport" to false
                ),
                "privacy" to mapOf(
                    "dataSharing" to false,
                    "analytics" to true
                ),
                "display" to mapOf(
                    "theme" to "auto",
                    "language" to "zh-CN"
                )
            )
            ResponseEntity.ok(ApiResponse.success(settings))
        } catch (e: Exception) {
            ResponseEntity.status(500)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 更新用户设置
     */
    @PutMapping("/settings")
    @Operation(summary = "更新用户设置", description = "更新用户的应用设置")
    fun updateUserSettings(
        @RequestBody settings: Map<String, Any>,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            // TODO: 实现用户设置更新逻辑
            ResponseEntity.ok(ApiResponse.success(settings))
        } catch (e: Exception) {
            ResponseEntity.status(500)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }

    /**
     * 测试接口 - 不需要认证
     */
    @GetMapping("/test")
    @Operation(summary = "测试接口", description = "测试接口", security = [])
    fun test(): ResponseEntity<ApiResponse<String>> {
        return ResponseEntity.ok(ApiResponse.success("测试接口响应成功 - Docker更新正常！"))
    }
    
    /**
     * 从认证对象中提取用户ID
     */
    private fun getUserIdFromAuth(authentication: Authentication): String {
        // 这里需要根据实际的认证实现来提取用户ID
        // 暂时返回一个示例值
        return "user-id-from-jwt"
    }
}
