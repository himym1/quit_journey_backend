package com.quitjourney.service

import com.quitjourney.dto.AchievementDto
import com.quitjourney.entity.Achievement
import com.quitjourney.entity.UserAchievement
import com.quitjourney.repository.AchievementRepository
import com.quitjourney.repository.UserAchievementRepository
import com.quitjourney.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * 成就服务接口
 */
interface AchievementService {
    fun getUserAchievements(userId: String): List<AchievementDto>
    fun getAvailableAchievements(userId: String): List<AchievementDto>
    fun unlockAchievement(userId: String, achievementKey: String): AchievementDto?
    fun checkAndUnlockAchievements(userId: String, triggerType: String, data: Map<String, Any>): List<AchievementDto>
    fun getAchievementStats(userId: String): Map<String, Any>
}

/**
 * 成就服务实现
 */
@Service
@Transactional
class AchievementServiceImpl(
    private val achievementRepository: AchievementRepository,
    private val userAchievementRepository: UserAchievementRepository,
    private val userRepository: UserRepository
) : AchievementService {
    
    override fun getUserAchievements(userId: String): List<AchievementDto> {
        val userUuid = UUID.fromString(userId)
        val userAchievements = userAchievementRepository.findByUserIdOrderByUnlockedAtDesc(userUuid)
        
        return userAchievements.map { userAchievement ->
            convertToAchievementDto(userAchievement.achievement!!, userAchievement.unlockedAt, userAchievement.progress)
        }
    }
    
    override fun getAvailableAchievements(userId: String): List<AchievementDto> {
        val userUuid = UUID.fromString(userId)
        val availableAchievements = userAchievementRepository.findUnlockedAchievementsByUserId(userUuid)
        
        return availableAchievements.map { achievement ->
            convertToAchievementDto(achievement)
        }
    }
    
    override fun unlockAchievement(userId: String, achievementKey: String): AchievementDto? {
        val userUuid = UUID.fromString(userId)
        val user = userRepository.findById(userUuid)
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        val achievement = achievementRepository.findByKey(achievementKey)
            ?: throw IllegalArgumentException("成就不存在")
        
        // 检查是否已解锁
        if (userAchievementRepository.existsByUserIdAndAchievementId(userUuid, achievement.id!!)) {
            return null // 已解锁
        }
        
        // 创建用户成就记录
        val userAchievement = UserAchievement().apply {
            this.user = user
            this.achievement = achievement
            unlockedAt = Instant.now()
        }
        
        userAchievementRepository.save(userAchievement)
        
        return convertToAchievementDto(achievement, userAchievement.unlockedAt, userAchievement.progress)
    }
    
    override fun checkAndUnlockAchievements(userId: String, triggerType: String, data: Map<String, Any>): List<AchievementDto> {
        val unlockedAchievements = mutableListOf<AchievementDto>()
        
        when (triggerType) {
            "checkin" -> {
                val streak = data["streak"] as? Int ?: 0
                val totalCheckins = data["totalCheckins"] as? Long ?: 0L
                
                // 检查连续打卡成就
                val streakAchievements = listOf(
                    "check_in_streak_7" to 7,
                    "check_in_streak_30" to 30
                )
                
                streakAchievements.forEach { (key, requiredStreak) ->
                    if (streak >= requiredStreak) {
                        unlockAchievement(userId, key)?.let { unlockedAchievements.add(it) }
                    }
                }
                
                // 检查总打卡数成就
                if (totalCheckins >= 100) {
                    unlockAchievement(userId, "total_checkins_100")?.let { unlockedAchievements.add(it) }
                }
            }
            
            "time_based" -> {
                val consecutiveDays = data["consecutiveDays"] as? Int ?: 0
                
                val timeBasedAchievements = listOf(
                    "first_day" to 1,
                    "one_week" to 7,
                    "half_month" to 15,
                    "one_month" to 30,
                    "three_months" to 90,
                    "half_year" to 180,
                    "one_year" to 365
                )
                
                timeBasedAchievements.forEach { (key, requiredDays) ->
                    if (consecutiveDays >= requiredDays) {
                        unlockAchievement(userId, key)?.let { unlockedAchievements.add(it) }
                    }
                }
            }
            
            "health_based" -> {
                val moneySaved = data["moneySaved"] as? Double ?: 0.0
                val cigarettesAvoided = data["cigarettesAvoided"] as? Int ?: 0
                
                // 检查节省金钱成就
                if (moneySaved >= 100) {
                    unlockAchievement(userId, "money_saved_100")?.let { unlockedAchievements.add(it) }
                }
                if (moneySaved >= 1000) {
                    unlockAchievement(userId, "money_saved_1000")?.let { unlockedAchievements.add(it) }
                }
                
                // 检查避免吸烟成就
                if (cigarettesAvoided >= 100) {
                    unlockAchievement(userId, "cigarettes_avoided_100")?.let { unlockedAchievements.add(it) }
                }
                if (cigarettesAvoided >= 1000) {
                    unlockAchievement(userId, "cigarettes_avoided_1000")?.let { unlockedAchievements.add(it) }
                }
            }
        }
        
        return unlockedAchievements
    }
    
    override fun getAchievementStats(userId: String): Map<String, Any> {
        val userUuid = UUID.fromString(userId)
        
        val totalAchievements = userAchievementRepository.countByUserId(userUuid)
        val totalPoints = userAchievementRepository.sumPointsByUserId(userUuid)
        val categoryStats = userAchievementRepository.findAchievementCountByCategoryAndUserId(userUuid)
        
        return mapOf(
            "totalAchievements" to totalAchievements,
            "totalPoints" to totalPoints,
            "categoryStats" to categoryStats.associate { it[0] as String to it[1] as Long }
        )
    }
    
    private fun convertToAchievementDto(
        achievement: Achievement, 
        unlockedAt: Instant? = null, 
        progress: Map<String, Any>? = null
    ): AchievementDto {
        // 获取中文名称和描述
        val name = achievement.nameI18n["zh-CN"] ?: achievement.nameI18n["en"] ?: achievement.key
        val description = achievement.descriptionI18n["zh-CN"] ?: achievement.descriptionI18n["en"] ?: ""
        
        return AchievementDto(
            id = achievement.id.toString(),
            key = achievement.key,
            name = name,
            description = description,
            iconName = achievement.iconName,
            category = achievement.category,
            points = achievement.points,
            unlockedAt = unlockedAt,
            progress = progress
        )
    }
}
