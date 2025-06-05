package com.quitjourney.repository

import com.quitjourney.entity.UserAchievement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * 用户成就数据访问接口
 */
@Repository
interface UserAchievementRepository : JpaRepository<UserAchievement, UUID> {
    
    /**
     * 查找用户的所有成就，按解锁时间降序排列
     */
    fun findByUserIdOrderByUnlockedAtDesc(userId: UUID): List<UserAchievement>
    
    /**
     * 检查用户是否已解锁指定成就
     */
    fun existsByUserIdAndAchievementId(userId: UUID, achievementId: UUID): Boolean
    
    /**
     * 根据用户ID和成就ID查找用户成就
     */
    fun findByUserIdAndAchievementId(userId: UUID, achievementId: UUID): UserAchievement?
    
    /**
     * 查找用户在指定时间范围内解锁的成就
     */
    fun findByUserIdAndUnlockedAtBetweenOrderByUnlockedAtDesc(
        userId: UUID, 
        startTime: Instant, 
        endTime: Instant
    ): List<UserAchievement>
    
    /**
     * 统计用户已解锁的成就数量
     */
    fun countByUserId(userId: UUID): Long
    
    /**
     * 统计用户已获得的总积分
     */
    @Query("""
        SELECT COALESCE(SUM(a.points), 0) 
        FROM UserAchievement ua 
        JOIN ua.achievement a 
        WHERE ua.user.id = :userId
    """)
    fun sumPointsByUserId(@Param("userId") userId: UUID): Long
    
    /**
     * 查找用户按分类的成就统计
     */
    @Query("""
        SELECT a.category, COUNT(ua) 
        FROM UserAchievement ua 
        JOIN ua.achievement a 
        WHERE ua.user.id = :userId 
        GROUP BY a.category
    """)
    fun findAchievementCountByCategoryAndUserId(@Param("userId") userId: UUID): List<Array<Any>>
    
    /**
     * 查找用户最近解锁的成就
     */
    fun findFirstByUserIdOrderByUnlockedAtDesc(userId: UUID): UserAchievement?
    
    /**
     * 删除用户的所有成就记录
     */
    fun deleteByUserId(userId: UUID): Int
    
    /**
     * 查找用户未解锁的成就
     */
    @Query("""
        SELECT a FROM Achievement a 
        WHERE a.isActive = true 
        AND a.id NOT IN (
            SELECT ua.achievement.id 
            FROM UserAchievement ua 
            WHERE ua.user.id = :userId
        )
        ORDER BY a.category, a.points
    """)
    fun findUnlockedAchievementsByUserId(@Param("userId") userId: UUID): List<com.quitjourney.entity.Achievement>
}
