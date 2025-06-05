package com.quitjourney.repository

import com.quitjourney.entity.SmokingRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * 吸烟记录数据访问接口
 */
@Repository
interface SmokingRecordRepository : JpaRepository<SmokingRecord, UUID> {
    
    /**
     * 查找用户的所有吸烟记录，按时间降序排列
     */
    fun findByUserIdOrderByTimestampDesc(userId: UUID): List<SmokingRecord>
    
    /**
     * 分页查找用户的吸烟记录
     */
    fun findByUserIdOrderByTimestampDesc(userId: UUID, pageable: Pageable): Page<SmokingRecord>
    
    /**
     * 查找用户在指定时间范围内的吸烟记录
     */
    fun findByUserIdAndTimestampBetweenOrderByTimestampDesc(
        userId: UUID, 
        startTime: Instant, 
        endTime: Instant
    ): List<SmokingRecord>
    
    /**
     * 统计用户总吸烟记录数
     */
    fun countByUserId(userId: UUID): Long
    
    /**
     * 统计用户在指定时间范围内的吸烟记录数
     */
    fun countByUserIdAndTimestampBetween(userId: UUID, startTime: Instant, endTime: Instant): Long
    
    /**
     * 统计用户总吸烟支数
     */
    @Query("SELECT COALESCE(SUM(s.cigarettesSmoked), 0) FROM SmokingRecord s WHERE s.user.id = :userId")
    fun sumCigarettesSmokedByUserId(@Param("userId") userId: UUID): Long
    
    /**
     * 统计用户在指定时间范围内的吸烟支数
     */
    @Query("""
        SELECT COALESCE(SUM(s.cigarettesSmoked), 0) 
        FROM SmokingRecord s 
        WHERE s.user.id = :userId AND s.timestamp BETWEEN :startTime AND :endTime
    """)
    fun sumCigarettesSmokedByUserIdAndTimestampBetween(
        @Param("userId") userId: UUID,
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): Long
    
    /**
     * 查找用户最近的吸烟记录
     */
    fun findFirstByUserIdOrderByTimestampDesc(userId: UUID): SmokingRecord?
    
    /**
     * 查找包含特定触发标签的记录
     */
    @Query("""
        SELECT s FROM SmokingRecord s 
        WHERE s.user.id = :userId AND :tag = ANY(s.triggerTags)
        ORDER BY s.timestamp DESC
    """)
    fun findByUserIdAndTriggerTagsContaining(
        @Param("userId") userId: UUID, 
        @Param("tag") tag: String
    ): List<SmokingRecord>
    
    /**
     * 统计用户各触发标签的使用频率
     */
    @Query("""
        SELECT tag, COUNT(*) as count
        FROM SmokingRecord s, unnest(s.triggerTags) as tag
        WHERE s.user.id = :userId
        GROUP BY tag
        ORDER BY count DESC
    """, nativeQuery = true)
    fun findTriggerTagStatsByUserId(@Param("userId") userId: UUID): List<Array<Any>>
    
    /**
     * 删除用户的所有吸烟记录
     */
    fun deleteByUserId(userId: UUID): Int
}
