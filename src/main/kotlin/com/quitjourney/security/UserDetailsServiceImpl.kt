package com.quitjourney.security

import com.quitjourney.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 用户详情服务实现
 * 
 * 为Spring Security提供用户认证信息
 */
@Service
@Transactional(readOnly = true)
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmailAndIsActive(email, true)
            ?: throw UsernameNotFoundException("用户不存在或已被禁用: $email")
        
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        
        return User.builder()
            .username(user.email)
            .password(user.passwordHash)
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(!user.isActive)
            .credentialsExpired(false)
            .disabled(!user.isActive)
            .build()
    }
}
