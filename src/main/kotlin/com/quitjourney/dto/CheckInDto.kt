package com.quitjourney.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate

/**
 * 创建打卡请求DTO
 */
data class CreateCheckInRequest(
    @field:NotNull(message = "打卡日期不能为空")
    val date: LocalDate,
    
    val checkinTime: Instant? = null,
    val note: String? = null
)

/**
 * 打卡记录响应DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckInDto(
    val id: String,
    val date: LocalDate,
    val isCheckedIn: Boolean,
    val checkinTime: Instant? = null,
    val streak: Int? = null,
    val note: String? = null,
    val achievements: List<AchievementDto>? = null,
    val createdAt: Instant
)

/**
 * 打卡统计DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckInStatsDto(
    val period: String,
    val totalDays: Int,
    val checkedInDays: Int,
    val checkinRate: Double,
    val currentStreak: Int,
    val longestStreak: Int,
    val streakHistory: List<StreakHistoryDto>? = null,
    val weeklyStats: List<WeeklyStatsDto>? = null
)

/**
 * 连续打卡历史DTO
 */
data class StreakHistoryDto(
    val date: LocalDate,
    val streak: Int
)

/**
 * 周统计DTO
 */
data class WeeklyStatsDto(
    val week: Int,
    val checkedIn: Int,
    val total: Int
)

/**
 * 成就DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AchievementDto(
    val id: String,
    val key: String,
    val name: String,
    val description: String,
    val iconName: String? = null,
    val category: String? = null,
    val points: Int,
    val unlockedAt: Instant? = null,
    val progress: Map<String, Any>? = null
)
