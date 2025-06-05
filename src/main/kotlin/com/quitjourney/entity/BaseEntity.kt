package com.quitjourney.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

/**
 * 基础实体类
 * 
 * 提供所有实体的通用字段：
 * - id: UUID主键
 * - createdAt: 创建时间
 * - updatedAt: 更新时间
 * 
 * 使用JPA审计功能自动管理时间字段
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    open var id: UUID? = null
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: Instant? = null
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    open var updatedAt: Instant? = null
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as BaseEntity
        return id != null && id == other.id
    }
    
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$id, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
