package com.quitjourney.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * 同步状态实体类
 * 
 * 记录用户设备的数据同步状态：
 * - 最后同步时间
 * - 同步版本号
 * - 设备信息
 * - 冲突解决策略
 */
@Entity
@Table(
    name = "sync_status",
    indexes = [
        Index(name = "idx_sync_status_user_id", columnList = "user_id"),
        Index(name = "idx_sync_status_last_sync", columnList = "last_sync_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_sync_status_user_device", columnNames = ["user_id", "device_id"])
    ]
)
class SyncStatus : BaseEntity() {
    
    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
    
    // 设备ID
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 255, message = "设备ID长度不能超过255个字符")
    @Column(name = "device_id", nullable = false, length = 255)
    var deviceId: String = ""
    
    // 最后同步时间
    @Column(name = "last_sync_at")
    var lastSyncAt: Instant? = null
    
    // 同步版本号
    @Column(name = "sync_version", nullable = false)
    var syncVersion: Long = 0
    
    // 客户端版本
    @Size(max = 20, message = "客户端版本长度不能超过20个字符")
    @Column(name = "client_version", length = 20)
    var clientVersion: String? = null
    
    // 冲突解决策略
    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_resolution_strategy", length = 20)
    var conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.SERVER_WINS
    
    override fun toString(): String {
        return "SyncStatus(id=$id, deviceId='$deviceId', lastSyncAt=$lastSyncAt, syncVersion=$syncVersion)"
    }
}

/**
 * 冲突解决策略枚举
 */
enum class ConflictResolutionStrategy {
    CLIENT_WINS,    // 客户端优先
    SERVER_WINS,    // 服务端优先
    LAST_WRITE_WINS, // 最后写入优先
    USER_CHOOSE     // 用户选择
}
