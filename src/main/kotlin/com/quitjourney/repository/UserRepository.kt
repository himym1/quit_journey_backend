package com.quitjourney.repository

import com.quitjourney.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * 用户数据访问接口
 * 
 * 提供用户相关的数据库操作：
 * - 基本CRUD操作
 * - 按邮箱查询
 * - 用户状态管理
 * - 登录时间更新
 */
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    
    /**
     * 根据邮箱查找用户
     */
    fun findByEmail(email: String): User?
    
    /**
     * 检查邮箱是否存在
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 根据邮箱和激活状态查找用户
     */
    fun findByEmailAndIsActive(email: String, isActive: Boolean): User?
    
    /**
     * 查找所有激活的用户
     */
    fun findByIsActive(isActive: Boolean): List<User>
    
    /**
     * 查找已验证邮箱的用户
     */
    fun findByEmailVerified(emailVerified: Boolean): List<User>
    
    /**
     * 查找在指定时间之后登录的用户
     */
    fun findByLastLoginAtAfter(lastLoginAt: Instant): List<User>
    
    /**
     * 更新用户最后登录时间
     */
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    fun updateLastLoginAt(@Param("userId") userId: UUID, @Param("lastLoginAt") lastLoginAt: Instant): Int
    
    /**
     * 统计激活用户数量
     */
    fun countByIsActive(isActive: Boolean): Long
    
    /**
     * 统计已验证邮箱的用户数量
     */
    fun countByEmailVerified(emailVerified: Boolean): Long
}
