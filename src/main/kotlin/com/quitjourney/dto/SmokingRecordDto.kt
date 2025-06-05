package com.quitjourney.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant

/**
 * 创建吸烟记录请求DTO
 */
data class CreateSmokingRecordRequest(
    @field:NotNull(message = "吸烟时间不能为空")
    val timestamp: Instant,
    
    @field:Min(value = 1, message = "吸烟数量必须大于0")
    val cigarettesSmoked: Int,
    
    val triggerTags: List<String>? = null,
    val notes: String? = null,
    val location: LocationDto? = null
)

/**
 * 更新吸烟记录请求DTO
 */
data class UpdateSmokingRecordRequest(
    val timestamp: Instant? = null,
    val cigarettesSmoked: Int? = null,
    val triggerTags: List<String>? = null,
    val notes: String? = null,
    val location: LocationDto? = null
)

/**
 * 位置信息DTO
 */
data class LocationDto(
    val name: String? = null,
    val coordinates: List<Double>? = null
)

/**
 * 吸烟记录响应DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SmokingRecordDto(
    val id: String,
    val timestamp: Instant,
    val cigarettesSmoked: Int,
    val triggerTags: List<String>? = null,
    val notes: String? = null,
    val location: LocationDto? = null,
    val impact: SmokingImpactDto? = null,
    val createdAt: Instant
)

/**
 * 吸烟影响DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SmokingImpactDto(
    val streakBroken: Boolean,
    val previousStreak: Int? = null,
    val moneyCost: Double? = null
)

/**
 * 吸烟统计DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SmokingStatsDto(
    val period: String,
    val totalRecords: Int,
    val totalCigarettes: Int,
    val totalCost: Double,
    val averagePerDay: Double,
    val commonTriggers: List<TriggerStatsDto>,
    val dailyBreakdown: List<DailySmokingDto>? = null,
    val trends: TrendComparisonDto? = null
)

/**
 * 触发因素统计DTO
 */
data class TriggerStatsDto(
    val tag: String,
    val count: Int
)

/**
 * 每日吸烟统计DTO
 */
data class DailySmokingDto(
    val date: String,
    val cigarettes: Int,
    val cost: Double
)

/**
 * 趋势对比DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TrendComparisonDto(
    val comparedToPreviousMonth: ComparisonDto
)

/**
 * 对比数据DTO
 */
data class ComparisonDto(
    val cigarettes: Int,
    val cost: Double,
    val improvement: Double
)
