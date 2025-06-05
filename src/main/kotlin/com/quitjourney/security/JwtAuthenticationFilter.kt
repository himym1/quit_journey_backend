package com.quitjourney.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT认证过滤器
 * 
 * 拦截请求，验证JWT令牌并设置安全上下文
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {
    
    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // 从请求头中获取JWT令牌
            val jwt = getJwtFromRequest(request)
            
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateAccessToken(jwt!!)) {
                // 从令牌中获取用户信息
                val userId = jwtTokenProvider.getUserIdFromToken(jwt)
                val email = jwtTokenProvider.getEmailFromToken(jwt)
                
                if (email != null) {
                    // 加载用户详情
                    val userDetails = userDetailsService.loadUserByUsername(email)
                    
                    // 创建认证对象
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    
                    // 设置安全上下文
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (ex: Exception) {
            logger.error("无法设置用户认证", ex)
        }
        
        filterChain.doFilter(request, response)
    }
    
    /**
     * 从请求头中提取JWT令牌
     */
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}
