package com.quitjourney.repository

import com.quitjourney.entity.Achievement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * 成就定义数据访问接口
 */
@Repository
interface AchievementRepository : JpaRepository<Achievement, UUID> {
    
    /**
     * 根据成就标识符查找成就
     */
    fun findByKey(key: String): Achievement?
    
    /**
     * 检查成就标识符是否存在
     */
    fun existsByKey(key: String): Boolean
    
    /**
     * 查找所有激活的成就
     */
    fun findByIsActiveOrderByCreatedAtAsc(isActive: Boolean): List<Achievement>
    
    /**
     * 根据分类查找成就
     */
    fun findByCategoryAndIsActiveOrderByPointsAsc(category: String, isActive: Boolean): List<Achievement>
    
    /**
     * 查找所有分类
     */
    fun findDistinctCategoryByIsActive(isActive: Boolean): List<String>
    
    /**
     * 统计激活的成就数量
     */
    fun countByIsActive(isActive: Boolean): Long
    
    /**
     * 根据分类统计成就数量
     */
    fun countByCategoryAndIsActive(category: String, isActive: Boolean): Long
}
