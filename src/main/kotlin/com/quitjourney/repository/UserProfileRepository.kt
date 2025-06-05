package com.quitjourney.repository

import com.quitjourney.entity.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * 用户资料数据访问接口
 */
@Repository
interface UserProfileRepository : JpaRepository<UserProfile, UUID> {
    
    /**
     * 根据用户ID查找用户资料
     */
    fun findByUserId(userId: UUID): UserProfile?
    
    /**
     * 检查用户是否有资料
     */
    fun existsByUserId(userId: UUID): Boolean
    
    /**
     * 查找有戒烟日期的用户资料
     */
    fun findByQuitDateIsNotNull(): List<UserProfile>
    
    /**
     * 查找在指定日期之后开始戒烟的用户
     */
    fun findByQuitDateAfter(quitDate: Instant): List<UserProfile>
    
    /**
     * 查找在指定日期范围内开始戒烟的用户
     */
    fun findByQuitDateBetween(startDate: Instant, endDate: Instant): List<UserProfile>
    
    /**
     * 统计已设置戒烟日期的用户数量
     */
    fun countByQuitDateIsNotNull(): Long
    
    /**
     * 根据用户ID删除用户资料
     */
    fun deleteByUserId(userId: UUID): Int
}
