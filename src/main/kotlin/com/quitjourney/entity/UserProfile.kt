package com.quitjourney.entity

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

/**
 * 用户资料实体类
 * 
 * 存储用户的戒烟相关详细信息：
 * - 基本个人信息
 * - 戒烟相关设置
 * - 地区和语言偏好
 */
@Entity
@Table(
    name = "user_profiles",
    indexes = [
        Index(name = "idx_user_profiles_user_id", columnList = "user_id"),
        Index(name = "idx_user_profiles_quit_date", columnList = "quit_date")
    ]
)
class UserProfile : BaseEntity() {
    
    // 关联用户
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User? = null
    
    // 基本信息
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    @Column(name = "name", length = 100)
    var name: String? = null
    
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @Column(name = "avatar_url", length = 500)
    var avatarUrl: String? = null
    
    @Size(max = 50, message = "时区长度不能超过50个字符")
    @Column(name = "timezone", length = 50)
    var timezone: String = "UTC"
    
    @Size(max = 10, message = "语言代码长度不能超过10个字符")
    @Column(name = "locale", length = 10)
    var locale: String = "zh-CN"
    
    // 戒烟相关信息
    @Column(name = "quit_date")
    var quitDate: Instant? = null
    
    @Column(name = "quit_reason", columnDefinition = "TEXT")
    var quitReason: String? = null
    
    @Min(value = 1, message = "每日吸烟量必须大于0")
    @Column(name = "cigarettes_per_day")
    var cigarettesPerDay: Int? = null
    
    @DecimalMin(value = "0.01", message = "香烟价格必须大于0")
    @Column(name = "cigarette_price", precision = 10, scale = 2)
    var cigarettePrice: BigDecimal? = null
    
    @Size(max = 3, message = "货币代码长度不能超过3个字符")
    @Column(name = "currency", length = 3)
    var currency: String = "CNY"
    
    override fun toString(): String {
        return "UserProfile(id=$id, name='$name', quitDate=$quitDate, cigarettesPerDay=$cigarettesPerDay)"
    }
}
