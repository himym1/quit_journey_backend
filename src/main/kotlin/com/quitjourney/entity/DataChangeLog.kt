package com.quitjourney.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.*

/**
 * 数据变更日志实体类
 * 
 * 记录所有数据变更操作，用于：
 * - 数据同步追踪
 * - 冲突检测和解决
 * - 审计追踪
 * - 数据恢复
 */
@Entity
@Table(
    name = "data_change_logs",
    indexes = [
        Index(name = "idx_data_change_logs_user_id", columnList = "user_id"),
        Index(name = "idx_data_change_logs_changed_at", columnList = "changed_at"),
        Index(name = "idx_data_change_logs_sync_version", columnList = "sync_version")
    ]
)
class DataChangeLog : BaseEntity() {
    
    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
    
    // 表名
    @NotBlank(message = "表名不能为空")
    @Size(max = 100, message = "表名长度不能超过100个字符")
    @Column(name = "table_name", nullable = false, length = 100)
    var tableName: String = ""
    
    // 记录ID
    @Column(name = "record_id", nullable = false)
    var recordId: UUID? = null
    
    // 操作类型
    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 10)
    var operation: DataOperation = DataOperation.INSERT
    
    // 变更前数据（JSON格式）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_data", columnDefinition = "jsonb")
    var oldData: Map<String, Any>? = null
    
    // 变更后数据（JSON格式）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", columnDefinition = "jsonb")
    var newData: Map<String, Any>? = null
    
    // 变更时间
    @Column(name = "changed_at", nullable = false)
    var changedAt: Instant = Instant.now()
    
    // 设备ID
    @Size(max = 255, message = "设备ID长度不能超过255个字符")
    @Column(name = "device_id", length = 255)
    var deviceId: String? = null
    
    // 同步版本号
    @Column(name = "sync_version")
    var syncVersion: Long? = null
    
    override fun toString(): String {
        return "DataChangeLog(id=$id, tableName='$tableName', recordId=$recordId, operation=$operation, changedAt=$changedAt)"
    }
}

/**
 * 数据操作类型枚举
 */
enum class DataOperation {
    INSERT,  // 插入
    UPDATE,  // 更新
    DELETE   // 删除
}
