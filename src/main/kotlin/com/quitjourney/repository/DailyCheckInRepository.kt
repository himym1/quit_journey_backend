package com.quitjourney.repository

import com.quitjourney.entity.DailyCheckIn
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

/**
 * 每日打卡数据访问接口
 */
@Repository
interface DailyCheckInRepository : JpaRepository<DailyCheckIn, UUID> {
    
    /**
     * 根据用户ID和日期查找打卡记录
     */
    fun findByUserIdAndCheckinDate(userId: UUID, checkinDate: LocalDate): DailyCheckIn?
    
    /**
     * 检查用户在指定日期是否已打卡
     */
    fun existsByUserIdAndCheckinDate(userId: UUID, checkinDate: LocalDate): Boolean
    
    /**
     * 查找用户的所有打卡记录，按日期降序排列
     */
    fun findByUserIdOrderByCheckinDateDesc(userId: UUID): List<DailyCheckIn>
    
    /**
     * 分页查找用户的打卡记录
     */
    fun findByUserIdOrderByCheckinDateDesc(userId: UUID, pageable: Pageable): Page<DailyCheckIn>
    
    /**
     * 查找用户在指定日期范围内的打卡记录
     */
    fun findByUserIdAndCheckinDateBetweenOrderByCheckinDateDesc(
        userId: UUID, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<DailyCheckIn>
    
    /**
     * 查找用户已打卡的记录
     */
    fun findByUserIdAndIsCheckedInOrderByCheckinDateDesc(userId: UUID, isCheckedIn: Boolean): List<DailyCheckIn>
    
    /**
     * 统计用户总打卡天数
     */
    fun countByUserIdAndIsCheckedIn(userId: UUID, isCheckedIn: Boolean): Long
    
    /**
     * 统计用户在指定日期范围内的打卡天数
     */
    fun countByUserIdAndIsCheckedInAndCheckinDateBetween(
        userId: UUID, 
        isCheckedIn: Boolean, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Long
    
    /**
     * 查找用户最近的连续打卡记录
     */
    @Query("""
        SELECT d FROM DailyCheckIn d 
        WHERE d.user.id = :userId AND d.isCheckedIn = true 
        ORDER BY d.checkinDate DESC
    """)
    fun findRecentCheckinsByUserId(@Param("userId") userId: UUID): List<DailyCheckIn>
    
    /**
     * 查找用户最后一次打卡记录
     */
    fun findFirstByUserIdOrderByCheckinDateDesc(userId: UUID): DailyCheckIn?
    
    /**
     * 删除用户的所有打卡记录
     */
    fun deleteByUserId(userId: UUID): Int
}
