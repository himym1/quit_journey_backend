package com.quitjourney.service

import com.quitjourney.dto.*
import com.quitjourney.entity.SmokingRecord
import com.quitjourney.repository.SmokingRecordRepository
import com.quitjourney.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 吸烟记录服务接口
 */
interface SmokingRecordService {
    fun createSmokingRecord(userId: String, request: CreateSmokingRecordRequest): SmokingRecordDto
    fun getSmokingRecords(userId: String, startTime: Instant?, endTime: Instant?, page: Int, limit: Int): PagedResponse<SmokingRecordDto>
    fun getSmokingRecord(userId: String, recordId: String): SmokingRecordDto?
    fun updateSmokingRecord(userId: String, recordId: String, request: UpdateSmokingRecordRequest): SmokingRecordDto
    fun deleteSmokingRecord(userId: String, recordId: String): Boolean
    fun getSmokingStats(userId: String, period: String, date: String?): SmokingStatsDto
}

/**
 * 吸烟记录服务实现
 */
@Service
@Transactional
class SmokingRecordServiceImpl(
    private val smokingRecordRepository: SmokingRecordRepository,
    private val userRepository: UserRepository
) : SmokingRecordService {
    
    override fun createSmokingRecord(userId: String, request: CreateSmokingRecordRequest): SmokingRecordDto {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        val smokingRecord = SmokingRecord().apply {
            this.user = user
            timestamp = request.timestamp
            cigarettesSmoked = request.cigarettesSmoked
            triggerTags = request.triggerTags?.toTypedArray()
            notes = request.notes
            location = request.location?.let { mapOf(
                "name" to (it.name ?: ""),
                "coordinates" to (it.coordinates ?: emptyList<Double>())
            )}
        }
        
        val savedRecord = smokingRecordRepository.save(smokingRecord)
        
        // 计算影响（如连续戒烟天数中断等）
        val impact = calculateSmokingImpact(user.id!!, savedRecord)
        
        return convertToSmokingRecordDto(savedRecord, impact)
    }
    
    override fun getSmokingRecords(userId: String, startTime: Instant?, endTime: Instant?, page: Int, limit: Int): PagedResponse<SmokingRecordDto> {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        val pageable = PageRequest.of(page - 1, limit)
        
        val recordsPage = if (startTime != null && endTime != null) {
            // 查询指定时间范围
            val records = smokingRecordRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                user.id!!, startTime, endTime
            )
            // 手动分页
            val total = records.size.toLong()
            val startIndex = (page - 1) * limit
            val endIndex = minOf(startIndex + limit, records.size)
            val pagedRecords = if (startIndex < records.size) records.subList(startIndex, endIndex) else emptyList()
            
            pagedRecords to total
        } else {
            // 查询所有记录
            val pagedResult = smokingRecordRepository.findByUserIdOrderByTimestampDesc(user.id!!, pageable)
            pagedResult.content to pagedResult.totalElements
        }
        
        val recordDtos = recordsPage.first.map { convertToSmokingRecordDto(it) }
        val pagination = PaginationInfo.of(page, limit, recordsPage.second)
        
        return PagedResponse.success(recordDtos, pagination)
    }
    
    override fun getSmokingRecord(userId: String, recordId: String): SmokingRecordDto? {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        val record = smokingRecordRepository.findById(UUID.fromString(recordId))
            .orElse(null) ?: return null
        
        if (record.user?.id != user.id) {
            throw IllegalArgumentException("无权限访问此记录")
        }
        
        return convertToSmokingRecordDto(record)
    }
    
    override fun updateSmokingRecord(userId: String, recordId: String, request: UpdateSmokingRecordRequest): SmokingRecordDto {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        val record = smokingRecordRepository.findById(UUID.fromString(recordId))
            .orElseThrow { IllegalArgumentException("记录不存在") }
        
        if (record.user?.id != user.id) {
            throw IllegalArgumentException("无权限修改此记录")
        }
        
        // 更新记录
        request.timestamp?.let { record.timestamp = it }
        request.cigarettesSmoked?.let { record.cigarettesSmoked = it }
        request.triggerTags?.let { record.triggerTags = it.toTypedArray() }
        request.notes?.let { record.notes = it }
        request.location?.let { locationDto ->
            record.location = mapOf(
                "name" to (locationDto.name ?: ""),
                "coordinates" to (locationDto.coordinates ?: emptyList<Double>())
            )
        }
        
        val updatedRecord = smokingRecordRepository.save(record)
        return convertToSmokingRecordDto(updatedRecord)
    }
    
    override fun deleteSmokingRecord(userId: String, recordId: String): Boolean {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        val record = smokingRecordRepository.findById(UUID.fromString(recordId))
            .orElse(null) ?: return false
        
        if (record.user?.id != user.id) {
            throw IllegalArgumentException("无权限删除此记录")
        }
        
        smokingRecordRepository.delete(record)
        return true
    }
    
    override fun getSmokingStats(userId: String, period: String, date: String?): SmokingStatsDto {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        // 计算时间范围
        val (startTime, endTime) = calculateTimeRange(period, date)
        
        // 统计数据
        val totalRecords = smokingRecordRepository.countByUserIdAndTimestampBetween(user.id!!, startTime, endTime).toInt()
        val totalCigarettes = smokingRecordRepository.sumCigarettesSmokedByUserIdAndTimestampBetween(user.id!!, startTime, endTime).toInt()
        
        // 计算平均每日吸烟量
        val days = java.time.Duration.between(startTime, endTime).toDays() + 1
        val averagePerDay = if (days > 0) totalCigarettes.toDouble() / days else 0.0
        
        // 获取触发因素统计
        val triggerStats = smokingRecordRepository.findTriggerTagStatsByUserId(user.id!!)
            .map { TriggerStatsDto(it[0] as String, (it[1] as Number).toInt()) }
        
        return SmokingStatsDto(
            period = date ?: period,
            totalRecords = totalRecords,
            totalCigarettes = totalCigarettes,
            totalCost = 0.0, // TODO: 根据用户设置的香烟价格计算
            averagePerDay = averagePerDay,
            commonTriggers = triggerStats
        )
    }
    
    private fun calculateSmokingImpact(userId: UUID, record: SmokingRecord): SmokingImpactDto? {
        // TODO: 实现吸烟影响计算逻辑
        // 比如计算是否中断了连续戒烟记录等
        return null
    }
    
    private fun calculateTimeRange(period: String, date: String?): Pair<Instant, Instant> {
        return when (period) {
            "month" -> {
                val targetDate = date?.let { LocalDate.parse("$it-01") } ?: LocalDate.now().withDayOfMonth(1)
                val startDate = targetDate.withDayOfMonth(1)
                val endDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth())
                startDate.atStartOfDay().toInstant(ZoneOffset.UTC) to endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)
            }
            "week" -> {
                val targetDate = date?.let { LocalDate.parse(it) } ?: LocalDate.now()
                val startDate = targetDate.minusDays(targetDate.dayOfWeek.value - 1L)
                val endDate = startDate.plusDays(6)
                startDate.atStartOfDay().toInstant(ZoneOffset.UTC) to endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)
            }
            else -> {
                val today = LocalDate.now()
                today.atStartOfDay().toInstant(ZoneOffset.UTC) to today.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)
            }
        }
    }
    
    private fun convertToSmokingRecordDto(record: SmokingRecord, impact: SmokingImpactDto? = null): SmokingRecordDto {
        return SmokingRecordDto(
            id = record.id.toString(),
            timestamp = record.timestamp,
            cigarettesSmoked = record.cigarettesSmoked,
            triggerTags = record.triggerTags?.toList(),
            notes = record.notes,
            location = record.location?.let { locationMap ->
                LocationDto(
                    name = locationMap["name"] as? String,
                    coordinates = locationMap["coordinates"] as? List<Double>
                )
            },
            impact = impact,
            createdAt = record.createdAt!!
        )
    }
}
