package com.quitjourney.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

/**
 * 每日打卡实体类
 * 
 * 记录用户每天的戒烟打卡情况：
 * - 打卡日期和时间
 * - 打卡状态
 * - 关联用户信息
 */
@Entity
@Table(
    name = "daily_checkins",
    indexes = [
        Index(name = "idx_daily_checkins_user_date", columnList = "user_id, checkin_date"),
        Index(name = "idx_daily_checkins_date", columnList = "checkin_date")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_daily_checkins_user_date", columnNames = ["user_id", "checkin_date"])
    ]
)
class DailyCheckIn : BaseEntity() {
    
    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
    
    // 打卡日期（只到日期，不包含时间）
    @Column(name = "checkin_date", nullable = false)
    var checkinDate: LocalDate = LocalDate.now()
    
    // 是否已打卡
    @Column(name = "is_checked_in", nullable = false)
    var isCheckedIn: Boolean = true
    
    // 打卡时间（具体的时间戳）
    @Column(name = "checkin_time")
    var checkinTime: Instant? = null
    
    override fun toString(): String {
        return "DailyCheckIn(id=$id, checkinDate=$checkinDate, isCheckedIn=$isCheckedIn, checkinTime=$checkinTime)"
    }
}
