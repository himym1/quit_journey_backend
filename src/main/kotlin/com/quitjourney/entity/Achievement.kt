package com.quitjourney.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

/**
 * 成就定义实体类
 * 
 * 定义系统中所有可获得的成就：
 * - 成就的基本信息
 * - 多语言支持
 * - 解锁条件配置
 * - 成就分类和积分
 */
@Entity
@Table(
    name = "achievements",
    indexes = [
        Index(name = "idx_achievements_key", columnList = "key"),
        Index(name = "idx_achievements_category", columnList = "category")
    ]
)
class Achievement : BaseEntity() {
    
    // 成就唯一标识符
    @NotBlank(message = "成就标识符不能为空")
    @Size(max = 100, message = "成就标识符长度不能超过100个字符")
    @Column(name = "key", nullable = false, unique = true, length = 100)
    var key: String = ""
    
    // 多语言名称（JSON格式）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "name_i18n", nullable = false, columnDefinition = "jsonb")
    var nameI18n: Map<String, String> = mapOf()
    
    // 多语言描述（JSON格式）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description_i18n", nullable = false, columnDefinition = "jsonb")
    var descriptionI18n: Map<String, String> = mapOf()
    
    // 图标名称
    @Size(max = 100, message = "图标名称长度不能超过100个字符")
    @Column(name = "icon_name", length = 100)
    var iconName: String? = null
    
    // 成就分类
    @Size(max = 50, message = "成就分类长度不能超过50个字符")
    @Column(name = "category", length = 50)
    var category: String? = null
    
    // 解锁条件（JSON配置）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unlock_condition", nullable = false, columnDefinition = "jsonb")
    var unlockCondition: Map<String, Any> = mapOf()
    
    // 成就积分
    @Column(name = "points")
    var points: Int = 0
    
    // 是否激活
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
    
    // 一对多关联用户成就
    @OneToMany(mappedBy = "achievement", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var userAchievements: MutableList<UserAchievement> = mutableListOf()
    
    override fun toString(): String {
        return "Achievement(id=$id, key='$key', category='$category', points=$points, isActive=$isActive)"
    }
}
