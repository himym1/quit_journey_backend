package com.quitjourney.service

import com.quitjourney.dto.*
import com.quitjourney.entity.User
import com.quitjourney.repository.UserRepository
import com.quitjourney.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 认证服务接口
 */
interface AuthService {
    fun register(request: RegisterRequest): AuthResponse
    fun login(request: LoginRequest): AuthResponse
    fun refreshToken(request: RefreshTokenRequest): TokenInfo
    fun logout(userId: String): Boolean
    fun forgotPassword(request: ForgotPasswordRequest): Boolean
    fun resetPassword(request: ResetPasswordRequest): Boolean
    fun verifyEmail(token: String): Boolean
}

/**
 * 认证服务实现
 */
@Service
@Transactional
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService
) : AuthService {
    
    override fun register(request: RegisterRequest): AuthResponse {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("邮箱已存在")
        }
        
        // 检查是否同意服务条款
        if (!request.agreeToTerms) {
            throw IllegalArgumentException("必须同意服务条款")
        }
        
        // 创建用户
        val user = User().apply {
            email = request.email
            passwordHash = passwordEncoder.encode(request.password)
            emailVerified = false
            isActive = true
        }
        
        val savedUser = userRepository.save(user)
        
        // 创建用户资料
        if (!request.name.isNullOrBlank()) {
            userService.createUserProfile(savedUser.id!!, request.name)
        }
        
        // 生成令牌
        val tokens = generateTokens(savedUser)
        
        // 更新最后登录时间
        userRepository.updateLastLoginAt(savedUser.id!!, Instant.now())
        
        return AuthResponse(
            user = convertToUserDto(savedUser),
            tokens = tokens
        )
    }
    
    override fun login(request: LoginRequest): AuthResponse {
        // 认证用户
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        
        // 获取用户信息
        val user = userRepository.findByEmailAndIsActive(request.email, true)
            ?: throw IllegalArgumentException("用户不存在或已被禁用")
        
        // 生成令牌
        val tokens = generateTokens(user)
        
        // 更新最后登录时间
        userRepository.updateLastLoginAt(user.id!!, Instant.now())
        
        return AuthResponse(
            user = convertToUserDto(user),
            tokens = tokens
        )
    }
    
    override fun refreshToken(request: RefreshTokenRequest): TokenInfo {
        // 验证刷新令牌
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw IllegalArgumentException("无效的刷新令牌")
        }
        
        // 获取用户信息
        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)
        val user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("用户不存在") }
        
        if (!user.isActive) {
            throw IllegalArgumentException("用户已被禁用")
        }
        
        // 生成新令牌
        return generateTokens(user)
    }
    
    override fun logout(userId: String): Boolean {
        // 这里可以实现令牌黑名单机制
        // 目前简单返回成功
        return true
    }
    
    override fun forgotPassword(request: ForgotPasswordRequest): Boolean {
        val user = userRepository.findByEmail(request.email)
            ?: return false // 不暴露用户是否存在
        
        // TODO: 实现发送重置密码邮件
        // 生成重置令牌并发送邮件
        
        return true
    }
    
    override fun resetPassword(request: ResetPasswordRequest): Boolean {
        // TODO: 实现密码重置逻辑
        // 验证重置令牌并更新密码
        
        return true
    }
    
    override fun verifyEmail(token: String): Boolean {
        // TODO: 实现邮箱验证逻辑
        // 验证邮箱验证令牌并更新用户状态
        
        return true
    }
    
    private fun generateTokens(user: User): TokenInfo {
        val accessToken = jwtTokenProvider.generateAccessToken(user.id.toString(), user.email)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id.toString())
        
        return TokenInfo(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpiration()
        )
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
    
    private fun convertToUserProfileDto(profile: com.quitjourney.entity.UserProfile): UserProfileDto {
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
