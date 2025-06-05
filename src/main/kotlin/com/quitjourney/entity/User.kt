package com.quitjourney.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * 用户实体类
 * 
 * 存储用户的基本认证信息：
 * - 邮箱和密码
 * - 邮箱验证状态
 * - 账户状态
 * - 最后登录时间
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_created_at", columnList = "created_at")
    ]
)
class User : BaseEntity() {
    
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    @Size(max = 255, message = "邮箱长度不能超过255个字符")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String = ""
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 60, max = 60, message = "密码哈希长度必须为60个字符")
    @Column(name = "password_hash", nullable = false, length = 60)
    var passwordHash: String = ""
    
    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false
    
    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null
    
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
    
    // 一对一关联用户资料
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var profile: UserProfile? = null
    
    // 一对多关联每日打卡
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var dailyCheckIns: MutableList<DailyCheckIn> = mutableListOf()
    
    // 一对多关联吸烟记录
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var smokingRecords: MutableList<SmokingRecord> = mutableListOf()
    
    // 一对多关联用户成就
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var userAchievements: MutableList<UserAchievement> = mutableListOf()
    
    override fun toString(): String {
        return "User(id=$id, email='$email', emailVerified=$emailVerified, isActive=$isActive)"
    }
}
