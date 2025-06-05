package com.quitjourney.controller

import com.quitjourney.dto.*
import com.quitjourney.service.DailyCheckInService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * 每日打卡控制器
 */
@RestController
@RequestMapping("/checkins")
@Tag(name = "每日打卡", description = "用户每日打卡相关API")
@SecurityRequirement(name = "bearerAuth")
class CheckInController(
    private val dailyCheckInService: DailyCheckInService
) {
    
    /**
     * 获取打卡记录
     */
    @GetMapping
    @Operation(summary = "获取打卡记录", description = "获取用户的打卡记录列表")
    fun getCheckIns(
        @Parameter(description = "开始日期") 
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        startDate: LocalDate?,
        
        @Parameter(description = "结束日期") 
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        endDate: LocalDate?,
        
        @Parameter(description = "页码，从1开始") 
        @RequestParam(defaultValue = "1") 
        page: Int,
        
        @Parameter(description = "每页数量") 
        @RequestParam(defaultValue = "31") 
        limit: Int,
        
        authentication: Authentication
    ): ResponseEntity<PagedResponse<CheckInDto>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val response = dailyCheckInService.getCheckIns(userId, startDate, endDate, page, limit)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * 创建打卡记录
     */
    @PostMapping
    @Operation(summary = "创建打卡记录", description = "为指定日期创建打卡记录")
    fun createCheckIn(
        @Valid @RequestBody request: CreateCheckInRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CheckInDto>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val checkIn = dailyCheckInService.createCheckIn(userId, request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(checkIn))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error("CHECKIN_ERROR", e.message ?: "打卡失败"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 获取指定日期的打卡记录
     */
    @GetMapping("/{date}")
    @Operation(summary = "获取指定日期打卡", description = "获取指定日期的打卡记录")
    fun getCheckInForDate(
        @Parameter(description = "日期") 
        @PathVariable 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        date: LocalDate,
        
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CheckInDto>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val checkIn = dailyCheckInService.getCheckInForDate(userId, date)
            
            if (checkIn != null) {
                ResponseEntity.ok(ApiResponse.success(checkIn))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 更新打卡记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新打卡记录", description = "更新指定的打卡记录")
    fun updateCheckIn(
        @Parameter(description = "打卡记录ID") 
        @PathVariable 
        id: String,
        
        @Parameter(description = "是否已打卡") 
        @RequestParam 
        isCheckedIn: Boolean,
        
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CheckInDto>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val checkIn = dailyCheckInService.updateCheckIn(userId, id, isCheckedIn)
            ResponseEntity.ok(ApiResponse.success(checkIn))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error("UPDATE_ERROR", e.message ?: "更新失败"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 删除打卡记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除打卡记录", description = "删除指定的打卡记录")
    fun deleteCheckIn(
        @Parameter(description = "打卡记录ID") 
        @PathVariable 
        id: String,
        
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val success = dailyCheckInService.deleteCheckIn(userId, id)
            
            if (success) {
                ResponseEntity.ok(ApiResponse.success("删除成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error("DELETE_ERROR", e.message ?: "删除失败"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 获取打卡统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取打卡统计", description = "获取用户的打卡统计数据")
    fun getCheckInStats(
        @Parameter(description = "统计周期：month, week") 
        @RequestParam(defaultValue = "month") 
        period: String,
        
        @Parameter(description = "指定日期，格式：YYYY-MM 或 YYYY-MM-DD") 
        @RequestParam(required = false) 
        date: String?,
        
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CheckInStatsDto>> {
        return try {
            val userId = getUserIdFromAuth(authentication)
            val stats = dailyCheckInService.getCheckInStats(userId, period, date)
            ResponseEntity.ok(ApiResponse.success(stats))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "服务器内部错误"))
        }
    }
    
    /**
     * 从认证对象中提取用户ID
     */
    private fun getUserIdFromAuth(authentication: Authentication): String {
        // TODO: 实现从JWT中提取用户ID的逻辑
        return "user-id-from-jwt"
    }
}
