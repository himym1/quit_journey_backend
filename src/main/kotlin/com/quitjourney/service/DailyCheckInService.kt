package com.quitjourney.service

import com.quitjourney.dto.*
import com.quitjourney.entity.DailyCheckIn
import com.quitjourney.entity.User
import com.quitjourney.repository.DailyCheckInRepository
import com.quitjourney.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * 每日打卡服务接口
 */
interface DailyCheckInService {
    fun createCheckIn(userId: String, request: CreateCheckInRequest): CheckInDto
    fun getCheckIns(userId: String, startDate: LocalDate?, endDate: LocalDate?, page: Int, limit: Int): PagedResponse<CheckInDto>
    fun getCheckInForDate(userId: String, date: LocalDate): CheckInDto?
    fun updateCheckIn(userId: String, checkInId: String, isCheckedIn: Boolean): CheckInDto
    fun deleteCheckIn(userId: String, checkInId: String): Boolean
    fun getCheckInStats(userId: String, period: String, date: String?): CheckInStatsDto
    fun getCurrentStreak(userId: String): Int
    fun getLongestStreak(userId: String): Int
}

/**
 * 每日打卡服务实现
 */
@Service
@Transactional
class DailyCheckInServiceImpl(
    private val dailyCheckInRepository: DailyCheckInRepository,
    private val userRepository: UserRepository,
    private val achievementService: AchievementService
) : DailyCheckInService {
    
    override fun createCheckIn(userId: String, request: CreateCheckInRequest): CheckInDto {
        val user = getUserById(userId)
        
        // 检查是否已经打卡
        val existingCheckIn = dailyCheckInRepository.findByUserIdAndCheckinDate(user.id!!, request.date)
        if (existingCheckIn != null) {
            throw IllegalArgumentException("今日已打卡")
        }
        
        // 创建打卡记录
        val checkIn = DailyCheckIn().apply {
            this.user = user
            checkinDate = request.date
            isCheckedIn = true
            checkinTime = request.checkinTime ?: Instant.now()
        }
        
        val savedCheckIn = dailyCheckInRepository.save(checkIn)
        
        // 计算连续打卡天数
        val currentStreak = calculateCurrentStreak(user.id!!)
        
        // 检查成就解锁
        val unlockedAchievements = achievementService.checkAndUnlockAchievements(userId, "checkin", mapOf(
            "streak" to currentStreak,
            "totalCheckins" to dailyCheckInRepository.countByUserIdAndIsCheckedIn(user.id!!, true)
        ))
        
        return convertToCheckInDto(savedCheckIn, currentStreak, unlockedAchievements)
    }
    
    override fun getCheckIns(userId: String, startDate: LocalDate?, endDate: LocalDate?, page: Int, limit: Int): PagedResponse<CheckInDto> {
        val user = getUserById(userId)
        val pageable = PageRequest.of(page - 1, limit)
        
        val checkInsPage = if (startDate != null && endDate != null) {
            // 查询指定日期范围
            val checkIns = dailyCheckInRepository.findByUserIdAndCheckinDateBetweenOrderByCheckinDateDesc(
                user.id!!, startDate, endDate
            )
            // 手动分页
            val total = checkIns.size.toLong()
            val startIndex = (page - 1) * limit
            val endIndex = minOf(startIndex + limit, checkIns.size)
            val pagedCheckIns = if (startIndex < checkIns.size) checkIns.subList(startIndex, endIndex) else emptyList()
            
            pagedCheckIns to total
        } else {
            // 查询所有记录
            val pagedResult = dailyCheckInRepository.findByUserIdOrderByCheckinDateDesc(user.id!!, pageable)
            pagedResult.content to pagedResult.totalElements
        }
        
        val checkInDtos = checkInsPage.first.map { convertToCheckInDto(it) }
        val pagination = PaginationInfo.of(page, limit, checkInsPage.second)
        
        return PagedResponse.success(checkInDtos, pagination)
    }
    
    override fun getCheckInForDate(userId: String, date: LocalDate): CheckInDto? {
        val user = getUserById(userId)
        val checkIn = dailyCheckInRepository.findByUserIdAndCheckinDate(user.id!!, date)
        return checkIn?.let { convertToCheckInDto(it) }
    }
    
    override fun updateCheckIn(userId: String, checkInId: String, isCheckedIn: Boolean): CheckInDto {
        val user = getUserById(userId)
        val checkIn = dailyCheckInRepository.findById(UUID.fromString(checkInId))
            .orElseThrow { IllegalArgumentException("打卡记录不存在") }
        
        if (checkIn.user?.id != user.id) {
            throw IllegalArgumentException("无权限修改此打卡记录")
        }
        
        checkIn.isCheckedIn = isCheckedIn
        val updatedCheckIn = dailyCheckInRepository.save(checkIn)
        
        return convertToCheckInDto(updatedCheckIn)
    }
    
    override fun deleteCheckIn(userId: String, checkInId: String): Boolean {
        val user = getUserById(userId)
        val checkIn = dailyCheckInRepository.findById(UUID.fromString(checkInId))
            .orElse(null) ?: return false
        
        if (checkIn.user?.id != user.id) {
            throw IllegalArgumentException("无权限删除此打卡记录")
        }
        
        dailyCheckInRepository.delete(checkIn)
        return true
    }
    
    override fun getCheckInStats(userId: String, period: String, date: String?): CheckInStatsDto {
        val user = getUserById(userId)
        
        // 根据period计算日期范围
        val (startDate, endDate, totalDays) = calculateDateRange(period, date)
        
        // 统计数据
        val checkedInDays = dailyCheckInRepository.countByUserIdAndIsCheckedInAndCheckinDateBetween(
            user.id!!, true, startDate, endDate
        ).toInt()
        
        val checkinRate = if (totalDays > 0) checkedInDays.toDouble() / totalDays else 0.0
        val currentStreak = getCurrentStreak(userId)
        val longestStreak = getLongestStreak(userId)
        
        return CheckInStatsDto(
            period = date ?: period,
            totalDays = totalDays,
            checkedInDays = checkedInDays,
            checkinRate = checkinRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )
    }
    
    override fun getCurrentStreak(userId: String): Int {
        val user = getUserById(userId)
        return calculateCurrentStreak(user.id!!)
    }
    
    override fun getLongestStreak(userId: String): Int {
        val user = getUserById(userId)
        val checkIns = dailyCheckInRepository.findByUserIdAndIsCheckedInOrderByCheckinDateDesc(user.id!!, true)
        
        if (checkIns.isEmpty()) return 0
        
        var maxStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null
        
        for (checkIn in checkIns.sortedBy { it.checkinDate }) {
            if (previousDate == null || checkIn.checkinDate == previousDate.plusDays(1)) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
            previousDate = checkIn.checkinDate
        }
        
        return maxStreak
    }
    
    private fun getUserById(userId: String): User {
        return userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
    }
    
    private fun calculateCurrentStreak(userId: UUID): Int {
        val recentCheckIns = dailyCheckInRepository.findRecentCheckinsByUserId(userId)
        
        if (recentCheckIns.isEmpty()) return 0
        
        var streak = 0
        var expectedDate = LocalDate.now()
        
        for (checkIn in recentCheckIns) {
            if (checkIn.checkinDate == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun calculateDateRange(period: String, date: String?): Triple<LocalDate, LocalDate, Int> {
        return when (period) {
            "month" -> {
                val targetDate = date?.let { LocalDate.parse("$it-01") } ?: LocalDate.now().withDayOfMonth(1)
                val startDate = targetDate.withDayOfMonth(1)
                val endDate = targetDate.withDayOfMonth(targetDate.lengthOfMonth())
                Triple(startDate, endDate, targetDate.lengthOfMonth())
            }
            "week" -> {
                val targetDate = date?.let { LocalDate.parse(it) } ?: LocalDate.now()
                val startDate = targetDate.minusDays(targetDate.dayOfWeek.value - 1L)
                val endDate = startDate.plusDays(6)
                Triple(startDate, endDate, 7)
            }
            else -> {
                val today = LocalDate.now()
                Triple(today, today, 1)
            }
        }
    }
    
    private fun convertToCheckInDto(checkIn: DailyCheckIn, streak: Int? = null, achievements: List<AchievementDto>? = null): CheckInDto {
        return CheckInDto(
            id = checkIn.id.toString(),
            date = checkIn.checkinDate,
            isCheckedIn = checkIn.isCheckedIn,
            checkinTime = checkIn.checkinTime,
            streak = streak,
            achievements = achievements,
            createdAt = checkIn.createdAt!!
        )
    }
}
