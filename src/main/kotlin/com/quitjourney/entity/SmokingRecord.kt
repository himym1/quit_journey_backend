package com.quitjourney.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

/**
 * 吸烟记录实体类
 * 
 * 记录用户的吸烟情况：
 * - 吸烟时间和数量
 * - 触发因素标签
 * - 备注信息
 * - 位置信息（可选）
 */
@Entity
@Table(
    name = "smoking_records",
    indexes = [
        Index(name = "idx_smoking_records_user_id", columnList = "user_id"),
        Index(name = "idx_smoking_records_timestamp", columnList = "timestamp"),
        Index(name = "idx_smoking_records_user_timestamp", columnList = "user_id, timestamp")
    ]
)
class SmokingRecord : BaseEntity() {
    
    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
    
    // 吸烟时间
    @Column(name = "timestamp", nullable = false)
    var timestamp: Instant = Instant.now()
    
    // 吸烟数量
    @Min(value = 1, message = "吸烟数量必须大于0")
    @Column(name = "cigarettes_smoked", nullable = false)
    var cigarettesSmoked: Int = 1
    
    // 触发因素标签（使用PostgreSQL数组类型）
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "trigger_tags", columnDefinition = "text[]")
    var triggerTags: Array<String>? = null
    
    // 备注
    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null
    
    // 位置信息（JSON格式存储）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location", columnDefinition = "jsonb")
    var location: Map<String, Any>? = null
    
    override fun toString(): String {
        return "SmokingRecord(id=$id, timestamp=$timestamp, cigarettesSmoked=$cigarettesSmoked, triggerTags=${triggerTags?.contentToString()})"
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        other as SmokingRecord
        return triggerTags?.contentEquals(other.triggerTags) ?: (other.triggerTags == null)
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (triggerTags?.contentHashCode() ?: 0)
        return result
    }
}
