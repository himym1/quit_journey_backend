package com.quitjourney.service

import com.quitjourney.dto.UserDto
import com.quitjourney.dto.UserProfileDto
import com.quitjourney.entity.User
import com.quitjourney.entity.UserProfile
import com.quitjourney.repository.UserProfileRepository
import com.quitjourney.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * 用户服务接口
 */
interface UserService {
    fun getUserProfile(userId: String): UserDto?
    fun updateUserProfile(userId: String, profileDto: UserProfileDto): UserDto
    fun createUserProfile(userId: UUID, name: String): UserProfile
    fun deleteUserAccount(userId: String): Boolean
    fun getUserStats(userId: String): Map<String, Any>
}

/**
 * 用户服务实现
 */
@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val dailyCheckInService: DailyCheckInService,
    private val smokingRecordService: SmokingRecordService
) : UserService {
    
    override fun getUserProfile(userId: String): UserDto? {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElse(null) ?: return null
        
        return convertToUserDto(user)
    }
    
    override fun updateUserProfile(userId: String, profileDto: UserProfileDto): UserDto {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        // 获取或创建用户资料
        val profile = userProfileRepository.findByUserId(user.id!!) 
            ?: UserProfile().apply { 
                this.user = user 
                userProfileRepository.save(this)
            }
        
        // 更新资料信息
        profile.apply {
            profileDto.quitDate?.let { quitDate = it }
            profileDto.quitReason?.let { quitReason = it }
            profileDto.cigarettesPerDay?.let { cigarettesPerDay = it }
            profileDto.cigarettePrice?.let { cigarettePrice = BigDecimal.valueOf(it) }
            currency = profileDto.currency
            timezone = profileDto.timezone
            locale = profileDto.locale
        }
        
        userProfileRepository.save(profile)
        
        // 重新加载用户数据
        val updatedUser = userRepository.findById(user.id!!).orElseThrow()
        return convertToUserDto(updatedUser)
    }
    
    override fun createUserProfile(userId: UUID, name: String): UserProfile {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        val profile = UserProfile().apply {
            this.user = user
            this.name = name
        }
        
        return userProfileRepository.save(profile)
    }
    
    override fun deleteUserAccount(userId: String): Boolean {
        return try {
            val userUuid = UUID.fromString(userId)
            
            // 删除用户资料（级联删除会处理相关数据）
            userProfileRepository.deleteByUserId(userUuid)
            
            // 删除用户
            userRepository.deleteById(userUuid)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getUserStats(userId: String): Map<String, Any> {
        val userUuid = UUID.fromString(userId)
        val profile = userProfileRepository.findByUserId(userUuid)
        
        val stats = mutableMapOf<String, Any>()
        
        // 计算戒烟天数
        profile?.quitDate?.let { quitDate ->
            val daysSinceQuit = java.time.Duration.between(quitDate, Instant.now()).toDays()
            stats["totalDaysQuit"] = daysSinceQuit
            
            // 计算节省的金钱
            profile.cigarettesPerDay?.let { cigarettesPerDay ->
                profile.cigarettePrice?.let { cigarettePrice ->
                    val totalMoneySaved = daysSinceQuit * cigarettesPerDay * cigarettePrice.toDouble()
                    stats["totalMoneySaved"] = totalMoneySaved
                }
            }
            
            // 计算避免的香烟数量
            profile.cigarettesPerDay?.let { cigarettesPerDay ->
                val totalCigarettesAvoided = daysSinceQuit * cigarettesPerDay
                stats["totalCigarettesAvoided"] = totalCigarettesAvoided
            }
        }
        
        // 获取最长连续打卡天数
        try {
            val longestStreak = dailyCheckInService.getLongestStreak(userId)
            stats["longestStreak"] = longestStreak
        } catch (e: Exception) {
            stats["longestStreak"] = 0
        }
        
        return stats
    }
    
    private fun convertToUserDto(user: User): UserDto {
        return UserDto(
            id = user.id.toString(),
            email = user.email,
            name = user.profile?.name,
            emailVerified = user.emailVerified,
            profile = user.profile?.let { convertToUserProfileDto(it) },
            createdAt = user.createdAt!!
        )
    }
    
    private fun convertToUserProfileDto(profile: UserProfile): UserProfileDto {
        return UserProfileDto(
            quitDate = profile.quitDate,
            quitReason = profile.quitReason,
            cigarettesPerDay = profile.cigarettesPerDay,
            cigarettePrice = profile.cigarettePrice?.toDouble(),
            currency = profile.currency,
            timezone = profile.timezone,
            locale = profile.locale
        )
    }
}
