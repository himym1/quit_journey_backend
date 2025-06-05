package com.quitjourney.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT令牌提供者
 * 
 * 负责JWT令牌的生成、验证和解析
 */
@Component
class JwtTokenProvider {
    
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${jwt.expiration}")
    private var jwtExpiration: Long = 900000 // 15分钟
    
    @Value("\${jwt.refresh-expiration}")
    private var jwtRefreshExpiration: Long = 2592000000 // 30天
    
    private val key: Key by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }
    
    /**
     * 生成访问令牌
     */
    fun generateAccessToken(userId: String, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)
        
        return Jwts.builder()
            .setSubject(userId)
            .claim("email", email)
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
    
    /**
     * 生成刷新令牌
     */
    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtRefreshExpiration)
        
        return Jwts.builder()
            .setSubject(userId)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
    
    /**
     * 从令牌中获取用户ID
     */
    fun getUserIdFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims.subject
    }
    
    /**
     * 从令牌中获取邮箱
     */
    fun getEmailFromToken(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims["email"] as? String
    }
    
    /**
     * 从令牌中获取令牌类型
     */
    fun getTokenTypeFromToken(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims["type"] as? String
    }
    
    /**
     * 获取令牌过期时间
     */
    fun getExpirationDateFromToken(token: String): Date {
        val claims = getClaimsFromToken(token)
        return claims.expiration
    }
    
    /**
     * 验证令牌是否有效
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            !isTokenExpired(claims)
        } catch (ex: JwtException) {
            false
        } catch (ex: IllegalArgumentException) {
            false
        }
    }
    
    /**
     * 验证访问令牌
     */
    fun validateAccessToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            val tokenType = claims["type"] as? String
            tokenType == "access" && !isTokenExpired(claims)
        } catch (ex: Exception) {
            false
        }
    }
    
    /**
     * 验证刷新令牌
     */
    fun validateRefreshToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            val tokenType = claims["type"] as? String
            tokenType == "refresh" && !isTokenExpired(claims)
        } catch (ex: Exception) {
            false
        }
    }
    
    /**
     * 获取访问令牌过期时间（毫秒）
     */
    fun getAccessTokenExpiration(): Long {
        return jwtExpiration
    }
    
    /**
     * 获取刷新令牌过期时间（毫秒）
     */
    fun getRefreshTokenExpiration(): Long {
        return jwtRefreshExpiration
    }
    
    /**
     * 从令牌中解析Claims
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
    
    /**
     * 检查令牌是否过期
     */
    private fun isTokenExpired(claims: Claims): Boolean {
        val expiration = claims.expiration
        return expiration.before(Date())
    }
}
