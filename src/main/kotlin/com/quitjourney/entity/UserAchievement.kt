package com.quitjourney.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

/**
 * 用户成就实体类
 * 
 * 记录用户已解锁的成就：
 * - 解锁时间
 * - 成就进度信息
 * - 关联用户和成就定义
 */
@Entity
@Table(
    name = "user_achievements",
    indexes = [
        Index(name = "idx_user_achievements_user_id", columnList = "user_id"),
        Index(name = "idx_user_achievements_unlocked_at", columnList = "unlocked_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_achievements_user_achievement", columnNames = ["user_id", "achievement_id"])
    ]
)
class UserAchievement : BaseEntity() {
    
    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
    
    // 关联成就定义
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    var achievement: Achievement? = null
    
    // 解锁时间
    @Column(name = "unlocked_at", nullable = false)
    var unlockedAt: Instant = Instant.now()
    
    // 成就进度信息（JSON格式）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "progress", columnDefinition = "jsonb")
    var progress: Map<String, Any>? = null
    
    override fun toString(): String {
        return "UserAchievement(id=$id, unlockedAt=$unlockedAt, progress=$progress)"
    }
}
